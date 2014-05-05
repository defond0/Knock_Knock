package com.example.knock_knock;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;



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
import android.os.IBinder;
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
				bufferSize = AudioRecord.getMinBufferSize(
		        		SAMPLE_RATE,
		        		AudioFormat.CHANNEL_IN_MONO,
		        		AudioFormat.ENCODING_PCM_16BIT);		
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
			
				//Set up float array for storing the ffted siganls as they are calculated
				FloatFFT fft = new FloatFFT(buffer.length);
				inputValuesMatrix=new float[numVectors][buffer.length*2];
				float[] audioBufferFFT;
				float[][] templateMatrix= getMatrixFromFile(new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_MUSIC).
						getAbsolutePath()+File.separator+curSound+".xml"), fft);
			
				
			
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
				
				while (on&&cur<rec){
					int M = n%numVectors;
							
					//read recorder
					long res = recorder.read(buffer, 0, buffer.length);
					//create audio event
					ae = new AudioEvent(tarForm, buffer.length);
					//Set overlap 
					ae.setOverlap(buffer.length/2);
					ae.setFloatBufferWithByteBuffer(buffer);
					
			
					//save floats and forward transform
					audioBufferFFT=ae.getFloatBuffer();
					fft.complexForward(audioBufferFFT);
					inputValuesMatrix[M]=audioBufferFFT;
					
				
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
					}
					if(n>64){
						//////CHANGE DETECTION THRESHOLD IN NORMALIZE DETECT.
						normalizeDetect(cV);
					}
					Cur_CONVO=sum(cV);
					windowSums[M]=Cur_CONVO;
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
		
			float[] v = new float[buffer.length*2];
			float f =0;
			float t =0;
			int k;
			for (int j = 0; j < buffer.length; ++j) {
				k=2*j;
				v[k]   = incoming[k]*template[k] - incoming[k+1]*template[k+1]; // real
				v[k+1] = incoming[k+1]*template[k] + incoming[k]*template[k+1]; // imaginary
			}
			fft.complexInverse(v, true);	
			for (int j = 0; j < buffer.length; ++j) {
					t=v[2*j];
					f+=t;
			}
			return f;
	}
	
	public double[] normalizeDetect(float[] f){
		double l = 0;
		double [] nD = new double[f.length];
		for (int k=0;k<f.length;k++){
			l += f[k]*f[k];
		}
		double a = Math.sqrt(l);
		for (int k=0;k<f.length;k++){
			nD[k]=f[k]/a;
			if(nD[k]>=.5){
				detected(curSound);
			}
			System.out.println(f[k]/a);
		}
		return nD;
	}
		
	public float[][] getMatrixFromFile(File f,FloatFFT fft){
		DataInputStream dis = makeDIS(f);
		AudioEvent ae = null;
		float[][] fl = new float[numVectors][buffer.length*2];
		float[] fttFl = new float[buffer.length];// = new float[buffer.length];
		byte[] read = new byte[bufferSize];
		for (int i=0;i<numVectors;i++){
				try {
					dis.read(read,i*buffer.length,buffer.length);
				} catch (IOException e) {
					System.out.println("xmltemplatereadinerror");
					e.printStackTrace();
				}
				//create audio event (this is used to get the float array for sound slice then forward fft
				ae = new AudioEvent(tarForm,buffer.length);
				ae.setOverlap(buffer.length/2);
				ae.setFloatBufferWithByteBuffer(read);
				System.out.println(read.length);
				System.out.println(fttFl.length);
				for (int k=0;k<buffer.length;k++)
				fttFl=ae.getFloatBuffer();
				
				fft.complexForward(fttFl);
				
				//save sound slice backwards for eventual cross correlation
				for(int k =0;k<buffer.length*2; k++){
					fl[i][k] = fttFl[(buffer.length-1)-k];
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




