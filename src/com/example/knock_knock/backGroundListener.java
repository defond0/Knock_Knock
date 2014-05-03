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
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;



public class backGroundListener extends Service  {
	//http://www.vogella.com/tutorials/AndroidServices/article.html
	
	final static boolean debug=true;
	final static int SAMPLE_RATE = 16000;
	private byte[] buffer;
	private int bufferSize;
	private be.hogent.tarsos.dsp.AudioFormat tarForm;
	private String curSound;
	public final static String EXTRA_MESSAGE = "com.example.backGroundList.MESSAGE";
	public final static String SOUND_NAME = "com.example.backGroundList.SOUNDNAME";
	public static final String PREFS_NAME = "KnockKnockPrefs";
	private String notification;
	private AudioRecord recorder;
	final static long recTime = 2500; 
	private int numVectors;
	private float[][] inputValuesMatrix;
	private SharedPreferences prefs;
	private long secs ;
	private long numSamples ;
	private int nS;
	private MFCC ap;
	private float Cur_CONVO;
	private float Prev_CONVO;
	private float avgConvo;
	private boolean on;
	private int Id;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		prefs = getSharedPreferences(PREFS_NAME, 0);
		Id=startId;
		on = PreferenceStorage.getON_OFF(prefs);
		Bundle b = intent.getExtras();
		curSound = "";
		System.out.println("on? "+on);
		if(on){
			if(b!=null){
				if(b.getString("sound") != null){
					curSound = b.getString("sound");
					Prev_CONVO=PreferenceStorage.getCurConvo(prefs,curSound);
					avgConvo=PreferenceStorage.getAvgConvo(prefs,curSound);	
					loadVariables();
					setupRecorder();	
					tarForm = getFormat();
					if((curSound!="")){
						listen();
						notification=curSound;
				}
			}
		}
		}
	    else{
	    	stopDelayed(Id);
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
		backGroundListener.this.stopService(new Intent(backGroundListener.this, backGroundListener.class));
		backGroundListener.super.onDestroy() ;
		
	}
	
	public void stopDelayed(int i){
		final Handler adamHandler = new Handler();
		final int id = i;
		adamHandler.postDelayed(new Runnable(){	
			public void run(){
				
				backGroundListener.this.stopSelf(id);
			}
		},recTime);
		
	}
	
	public void loadVariables(){
		secs = recTime/1000;
		bufferSize = AudioRecord.getMinBufferSize(
        		SAMPLE_RATE,
        		AudioFormat.CHANNEL_IN_MONO,
        		AudioFormat.ENCODING_PCM_16BIT);
		numSamples = secs*SAMPLE_RATE;
		nS = (int)(long)numSamples;
		numVectors =(((nS/bufferSize)+1)*2)+12;
	}
	
	public void detected(){
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
			i.putExtra("notification", notification);
			startActivity(i);
		}
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		PreferenceStorage.setON_OFF(prefs,false);
	}
	
	private void listen(){	
		
		Thread listen = new Thread(new Runnable(){
			@Override
			public void run(){
				
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
				Cur_CONVO=0;
				//Set up float array for storing the mfccs as they are calculated
				inputValuesMatrix=new float[numVectors][32];
				float[][] templateMatrix= getMatrixFromFile(new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_MUSIC).
						getAbsolutePath()+File.separator+curSound+".xml"));
				
				//Set up mfcc extractor
				ap = new MFCC(bufferSize/2,SAMPLE_RATE);
				
				AudioEvent ae = null;
			
				//start recording
				recorder.startRecording();
				
				//Recording Loop
				long cur = System.currentTimeMillis();
				System.out.println(curSound+ " Starting at: "+cur);
				long rec = cur + (recTime*2);
				int cc = 0;
				while (cur<rec){
					if(on && cc==numVectors/2){
						System.out.println("Spawning new intent "+curSound+"Listener at "+System.currentTimeMillis());
						Intent i = new Intent(backGroundListener.this, backGroundListener.class);
						i.putExtra("sound",curSound);
						backGroundListener.this.startService(i);
					}
					
					//read recorder
					long res = recorder.read(buffer, 0, buffer.length);
					
					//create audio event
					ae = new AudioEvent(tarForm, res);
					
					//Set overlap (this needs work)
					ae.setOverlap(buffer.length/4);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//use TarsosDsp MFCC to process signal
					ap.process(ae);
					
					
					int c = cc%numVectors;
					//save 30 floats that MFCC returns
					for (int k=0;k<30;k++){
						inputValuesMatrix[c][k]=ap.getMFCC()[k];
					}
					inputValuesMatrix[c][30]= 0; //zero padding
					inputValuesMatrix[c][31]= 0; //zero padding
					//for (int q=0;q<numVectors;q++){
						convoluteFFT(inputValuesMatrix[c],templateMatrix[c]);
					//}
					PreferenceStorage.setCurConvo(prefs,curSound,Cur_CONVO);
					cc+=1;	
					cur = System.currentTimeMillis();					
				}
				
				/*
				 * this is where it happens, do we detect or dont we?
				 */
				
				
				float ccDif = Cur_CONVO-avgConvo;
				float pDif = Prev_CONVO-avgConvo;
				float avgDif = ((pDif+ccDif)/2);
				if(avgDif>0&&ccDif>0&&pDif>0){
					if((ccDif>=2*avgDif)){
						System.out.println("detected");
						detected();
						stopService(new Intent(backGroundListener.this, backGroundListener.class));
						stopSelf(Id);
					
					}
					if(debug){
//						System.out.println("done at: "+cur);
//						System.out.println("PrevConvo: " + Prev_CONVO);
//						System.out.println("pDif: " + pDif);
//						System.out.println("CurConvo: "+Cur_CONVO);
//						System.out.println("ccDif: " + ccDif);
//						System.out.println("avg Convo: "+avgConvo);
//						System.out.println("avgDif: " + (ccDif+pDif/2));
//						System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~");
						System.out.println("positive diffs");
					}
					SharedPreferences p = getSharedPreferences(PREFS_NAME, 0);
					avgConvo = (Prev_CONVO+Cur_CONVO)/2;
					PreferenceStorage.setAverageConvo(p,curSound ,avgConvo);
				}
				
				recorder.stop();			
				recorder.release();
				recorder = null;
				stopDelayed(Id);
			}
		});
		listen.run();
		listen.interrupt();
		listen=null;
	}
	
	public void convoluteFFT(float [] incoming, float[]template){
		
		//NOTE TEMPLATE MUST BE ALREADY HAVE BEEN FORWARDFTT INTRAINING
		FloatFFT fft = new FloatFFT(32);
		//Set up fft 
		float[] audioBufferFFT;
		audioBufferFFT= new float[64];
	
			// Convolution via FFT
			// 1. data
			for (int j = 0; j < 32; ++j) {
				audioBufferFFT[2*j]=incoming[j];
				audioBufferFFT[2*j+1] = 0;
			}
			fft.complexForward(audioBufferFFT);

			
			// 2.convolution via complex multiplication
			float[] v = new float[64];
			for(int k=0;k<numVectors;k++){
				for (int j = 0; j < 32; ++j) {
					v[2*j]   = audioBufferFFT[2*j]*template[2*j] - audioBufferFFT[2*j+1]*template[2*j+1]; // real
					v[2*j+1] = audioBufferFFT[2*j+1]*template[2*j] + audioBufferFFT[2*j]*template[2*j+1]; // imaginary
				}
				fft.complexInverse(v, true);
				for (int j = 0; j < 32; ++j) {
					Cur_CONVO+=v[2*j];
				}
			}
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

	public void setupRecorder(){
	    buffer = new byte[bufferSize];
		recorder = new AudioRecord(
	        		MediaRecorder.AudioSource.MIC,
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_IN_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT,
	        		bufferSize);
		 
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

	
}




