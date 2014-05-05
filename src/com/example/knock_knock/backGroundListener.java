package com.example.knock_knock;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;



import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.mfcc.MFCC;
import be.hogent.tarsos.dsp.util.fft.FloatFFT;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;



public class backGroundListener extends Service  {
	//http://www.vogella.com/tutorials/AndroidServices/article.html
	
	final static boolean debug=true;
	final static int SAMPLE_RATE = 16000;
	private int bufferSize;
	private String curSound;
	public final static String EXTRA_MESSAGE = "com.example.backGroundList.MESSAGE";
	public final static String SOUND_NAME = "com.example.backGroundList.SOUNDNAME";
	public static final String PREFS_NAME = "KnockKnockPrefs";
	final static long recTime = 2500; 
	final static int numVectors = 64;
	private float[][] inputValuesMatrix;
	private SharedPreferences prefs;
	private MFCC ap;
	private float Cur_CONVO;
	private boolean on;
	final int numListens = 2;
	final int overlap = numListens/2-1;
	byte[] buffer;
	float[] windowSums;
	AudioRecord recorder;
	be.hogent.tarsos.dsp.AudioFormat tarForm;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = getSharedPreferences(PREFS_NAME, 0);
		on = PreferenceStorage.getON_OFF(prefs);
		Bundle b = intent.getExtras();
		curSound = "";
		if(on){
			if(b!=null){
				if(b.getString("sound") != null){
					curSound = b.getString("sound");
					bufferSize = AudioRecord.getMinBufferSize(
			        		SAMPLE_RATE,
			        		AudioFormat.CHANNEL_IN_MONO,
			        		AudioFormat.ENCODING_PCM_16BIT);		
					if((curSound!="")){				
						launchControlThread();
				}
			}
		}
		}
	    else{
	    	stopSelf();
	    }
		return super.onStartCommand(intent,flags,startId);
	  }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy(){
		System.out.println("ding");
		backGroundListener.super.onDestroy() ;
		
	}
	
	
	public void detected(String sound){
		boolean alert = PreferenceStorage.isAlertNotifOn(prefs, curSound);
		boolean vibe = PreferenceStorage.isVibrateNotifOn(prefs, curSound);
	
		if(vibe){
			Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
			// Vibrate for 500 milliseconds
			v.vibrate(200);
		}
		if(alert){
			Intent i = new Intent(this, Notification_Screen.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra("notification", sound);
			startActivity(i);
		}
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		PreferenceStorage.setON_OFF(prefs,false);
	}
	
	private void listen(){	
		
		///listens to ten seconds of audio input and convolutes it with the training template
		Thread listen = new Thread(new Runnable(){
			@Override
			public void run(){
				windowSums= new float[64];
				//set up recorder
				buffer = new byte[bufferSize];
				recorder = new AudioRecord(
				        		MediaRecorder.AudioSource.MIC,
				        		SAMPLE_RATE,
				        		AudioFormat.CHANNEL_IN_MONO,
				        		AudioFormat.ENCODING_PCM_16BIT,
				        		bufferSize);
				tarForm = getFormat();
				//start recording
				recorder.startRecording();
			
				//Set up float array for storing the mfccs as they are calculated
				inputValuesMatrix=new float[numVectors][64];
				float[] audioBufferFFT;
				float[][] templateMatrix= getMatrixFromFile(new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_MUSIC).
						getAbsolutePath()+File.separator+curSound+".xml"));
				//float[][] templateMatrix = new float[64][64];
				
				//Set up mfcc extractor
				ap = new MFCC(bufferSize/2,SAMPLE_RATE);
				AudioEvent ae = null;
			
				//Recording Loop
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
				Cur_CONVO=0;//PreferenceStorage.getCurConvo(prefs,curSound);
				long cur = System.currentTimeMillis();
				System.out.println(curSound+ " Starting at: "+cur);
				long rec = cur + (recTime*5);
				int n = 0;
				double tmp;
				int fx,gx;
				FloatFFT fft = new FloatFFT(32);
				float averageCur_CONVO=Float.POSITIVE_INFINITY;
				while (on&&cur<rec){
					int M = n%numVectors;
							
					//read recorder
					long res = recorder.read(buffer, 0, buffer.length);
					//create audio event
					ae = new AudioEvent(tarForm, res);
					//Set overlap 
					ae.setOverlap(buffer.length/2);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//use TarsosDsp MFCC to process signal
					ap.process(ae);
			
					//save 30 floats that MFCC returns and ffts them
					
					audioBufferFFT=ap.getMFCC();
					for (int j = 0; j < 32; ++j) {
						if(j<=29){
							inputValuesMatrix[M][2*j]=audioBufferFFT[j];
						}
						else{
							inputValuesMatrix[M][2*j]=0; //zero padding
						}
							inputValuesMatrix[M][2*j+1]=0;
						
					}
					fft.complexForward(inputValuesMatrix[M]);
					
					
					// Convolution via complex multiplixation 
					// http://en.wikipedia.org/wiki/Convolution   this is mostly looking at the section on discrete convolution and circular discrete convolution
					//float nSum = 0;
					tmp=0;
					float[]cV = new float [2*M];
					for (int m=-1*M; m<M;m++){
						fx = Math.abs(m%M);
						gx = Math.abs((M-m)%M);
						tmp=complexMultSumFFT(inputValuesMatrix[fx],templateMatrix[gx],fft); // perhaps reverse template?
						cV[fx]=(float) tmp;
						//tmp/=(64)-Math.abs((fx*2)-64+1);
						//nSum+=tmp;
						
					}
					normalize(cV);
					Cur_CONVO=sum(cV);
					
					
					
					
					///////////STATS///////
					
					windowSums[M]=Cur_CONVO;
					//double [] nW = normalize(windowSums);
					
					
					
					/////DETECTION ZONE TMP == CONVOLUTI0N(n)
					if(Cur_CONVO>=10000000){
						detected(curSound);
					}
					else{
					PreferenceStorage.setCurConvo(prefs,curSound,Cur_CONVO);
					}
					//System.out.println("cc "+ Cur_CONVO);
					on = PreferenceStorage.getON_OFF(prefs);
					n+=1;	
					cur = System.currentTimeMillis();					
				}
			
				recorder.stop();			
				recorder.release();
				recorder = null;
				on = PreferenceStorage.getON_OFF(prefs);
				stopSelf();
				
			}
		});
		listen.run();
		listen.interrupt();
		listen=null;
	}
	
	public float complexMultSumFFT(float [] incoming, float[]template, FloatFFT fft){
		
		//NOTE TEMPLATE MUST BE ALREADY HAVE BEEN FORWARDFTT INTRAINING 
		
			float[] v = new float[64];
			float f =0;
			float t =0;
			int k;
			for (int j = 0; j < 32; ++j) {
				k=62-2*j;
				v[2*j]   = incoming[2*j]*template[k] - incoming[2*j+1]*template[k+1]; // real
				v[2*j+1] = incoming[2*j+1]*template[k] + incoming[2*j]*template[k+1]; // imaginary
			}
			fft.complexInverse(v, true);	
			for (int j = 0; j < 32; ++j) {
					t=v[2*j];
					f+=t;
			}
			
			return f;
	}
	
	public double[] normalize(float[] f){
		double l = 0;
		double [] nD = new double[f.length];
		for (int k=0;k<f.length;k++){
			l += f[k]*f[k];
		}
		double a = Math.sqrt(l);
		for (int k=0;k<f.length;k++){
			nD[k]=f[k]/a;
			System.out.println(f[k]/a);
		}
		return nD;
	}
		
	public float[][] getMatrixFromFile(File f){
		DataInputStream dis = makeDIS(f);
		float[][] fl = new float[numVectors][64];
		
		for (int i=0;i<numVectors;i++){
			for (int j=0; j<64;j++){
				try {
					fl[i][j]=dis.readFloat();
				} catch (IOException e) {
					System.out.println("getMatrixFromFile Error"+numVectors+", 64" +"in ReadFloat at "+ i+", "+j);
					//e.printStackTrace();
				}
			}
		}
		try {
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fl;
	}
	
	public be.hogent.tarsos.dsp.AudioFormat getFormat(){
		be.hogent.tarsos.dsp.AudioFormat aF = new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE,16,1,true,false);
		return aF;
		
	}
	
	public DataInputStream makeDIS(File f){
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(f));
		} catch (FileNotFoundException e1) {
			System.out.println("DIS error with " + f.getPath());
			e1.printStackTrace();
		}
		return dis;
		
	}
	
	public float sum(float[] f1){
		float s = 0;
		for (int i=0;i<f1.length;i++){
			s+=f1[i];
		}
		return s;
	}

	private void launchControlThread(){
	//Start numlistening listening Threads (which will each listen to 10 seconds of incoming audio, then kill the background listener, after starting a new one at the half way 
	//point.
	final Handler adamHandler = new Handler();
	for (int c=0; c<numListens;c++){
		on = PreferenceStorage.getON_OFF(prefs);
		final long listenCount = c;
		long offset = c*10000;
		adamHandler.postDelayed(new Runnable(){	
			public void run(){
				backGroundListener.this.listen();
				if(on){
					if(listenCount==(numListens-1)){ //start new bglistener
						Intent i = new Intent(backGroundListener.this, backGroundListener.class);
						i.putExtra("sound",curSound);
						backGroundListener.this.startService(i);
						backGroundListener.this.stopSelf();
					}
				}
				else{
					backGroundListener.this.stopSelf();
				}
			}
		},offset);
	}
	
}
}




