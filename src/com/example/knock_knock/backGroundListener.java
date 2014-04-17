package com.example.knock_knock;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.mfcc.MFCC;
import be.hogent.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;



public class backGroundListener extends Service {
	//http://www.vogella.com/tutorials/AndroidServices/article.html
	
	
	final static int SAMPLE_RATE = 16000;
	private byte[] buffer;
	private int bufferSize;
	private be.hogent.tarsos.dsp.AudioFormat tarForm;
	private boolean rec;
	private Set<String> checkedSounds;
	public final static String EXTRA_MESSAGE = "com.example.backGroundList.MESSAGE";
	public final static String SOUND_NAME = "com.example.backGroundList.SOUNDNAME";
	public static final String PREFS_NAME = "KnockKnockPrefs";
	private String notification;
	private AudioRecord recorder;
	final static long recTime = 5000; //just here for now TODO place in prefs
	private int numWindows;
	private float[][] inputValues;
	private HashMap <String,float[][]> soundIndex;
	private HashMap <String,float[]> curDots;
	private HashMap <String,float[]> averageConvolution;
	private SharedPreferences prefs;
	private float convolution;
	private static double THRESHOLD =2.25;
	private float stDev,avg;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //TODO do something useful
		rec = true;
		//Get SharedPreferences and loud up sounds
		prefs = getSharedPreferences(PREFS_NAME, 0);
		checkedSounds = PreferenceStorage.getAllCheckedSounds(prefs);
		prefs = getSharedPreferences(PREFS_NAME, 0);
		listen();
		return super.onStartCommand(intent,flags,startId);
	  }
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void notify(String sound){
		boolean alert = true;//PreferenceStorage.isAlertNotifOn(prefs, sound);
		boolean push = true;//PreferenceStorage.isPushNotifOn(prefs,sound);
		boolean vibe = true;// PreferenceStorage.isVibrateNotifOn(prefs, sound);
		if(alert){
			System.out.println("Alert");
			Intent i = new Intent(this, Notification_Screen.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.putExtra(SOUND_NAME, sound);
			startActivity(i);
		}
		if(push){
			System.out.println("PUSH     to Do");
		}
		if(vibe){
			System.out.println("VIBE     to Do");
		}
		this.stopSelf();
	}
	
	private void listen(){	
		
		Thread listen = new Thread(new Runnable(){
			@Override
			public void run(){	
				//set up recorder
				setupRecorder();
				
				//Set up mfcc extractor
				MFCC ap = new MFCC(buffer.length/2,SAMPLE_RATE);
				tarForm = getFormat();
				AudioEvent ae = null;
				
				//Set up float array for storing the mfccs as they are calculated
				long secs = recTime/1000;
				long numSamples = secs*SAMPLE_RATE;
				int nS = (int)(long)numSamples;
				numWindows =(((nS/bufferSize)+1)*2);
				inputValues=new float[numWindows][30];
				loadSoundIndex();

				//start recording
				recorder.startRecording();
				
				//Recording Loop
				int cc = 0;
				int i =0;
				while (rec){
					//read recorder
					if(i==0){
						System.out.println("Start of Window "+cc+" at "+System.currentTimeMillis());
					}
					long res = recorder.read(buffer, 0, buffer.length);
					
					//create audio event
					ae = new AudioEvent(tarForm, res);
					
					//Set over lap (this needs work)
					ae.setOverlap(buffer.length/4);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//use TarsosDsp MFCC to process signal
					ap.process(ae);
					
					//save 30 floats that MFCC returns
					inputValues[i]=ap.getMFCC();
					
					//get dot product of newly 
					detectDotProducts(i);
					
					//loop maintenance 
					i+=1;
					if(!(i<numWindows)){
						
						Iterator<String> soundIter = checkedSounds.iterator();
						float[] v = new float[numWindows];
						while(soundIter.hasNext()){
							String curSound = soundIter.next();
							v = curDots.get(curSound);
							convolution = sum(v);
							float[] f=backGroundListener.this.averageConvolution.get(curSound);
							
							float[] shift = new float[f.length-1];
							for(i=0;i<shift.length;i++){
								shift[i]=f[i];
							}
							
							f[0] = convolution;
							for (i=0;i<shift.length;i++){
								f[i+1]=shift[i];
							}	
														
							float avg = avg(f,cc);
							stDev = stDev(f,cc);
							if(((convolution-avg)>=stDev*THRESHOLD)&&cc>4){
								System.out.println("Detected at "+System.currentTimeMillis());
								backGroundListener.this.notify(curSound);
								SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
								PreferenceStorage.setIsRec(prefs,false);	
							}
											
							System.out.println("Current dotProduct for sound " +curSound + 
									" sum over window "+cc+": "+convolution +" stDev: "+stDev+" average: "+avg);
							backGroundListener.this.averageConvolution.put(curSound,f);
							cc+=1;
							i=0;
						}
					}
					rec=PreferenceStorage.getIsRec(prefs);
				}
				recorder.stop();
				recorder.release();
				recorder = null;
			}
		});
		listen.run();
	}
	
	public void loadSoundIndex(){
		Thread load = new Thread(new Runnable(){
			@Override
			public void run(){	
				HashMap <String,float[][]> tmpDex = new HashMap<String,float[][]>();
				HashMap <String,float[]> tmpDots = new HashMap<String,float[]>();
				HashMap <String,float[]> tmpAvg = new HashMap<String,float[]>();
				//Load sound selection preferences
				Iterator<String> soundIter = checkedSounds.iterator();
				while(soundIter.hasNext()){
					String curSound = soundIter.next();
					File curFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).
							getAbsolutePath()+File.separator+curSound+".xml");
					tmpDex.put(curSound,getMatrixFromFile(curFile));
					tmpDots.put(curSound, new float[numWindows]);
					tmpAvg.put(curSound, new float[3]);
				}
				soundIndex = tmpDex;
				curDots = tmpDots;
				averageConvolution = tmpAvg;
			}
		});
		load.run();
	}
	
	public void detectDotProducts(int i){
		float [][] fl = new float[numWindows][30];//soundIndex.get(curSound);
		float [] f =  new float[numWindows];
		float dot = 0;
		Iterator<String> soundIter = checkedSounds.iterator();
		while(soundIter.hasNext()){
			String curSound = soundIter.next();
			fl = soundIndex.get(curSound);
			f = curDots.get(curSound);
			dot = dotProduct(fl[i],inputValues[i]);
			f[i] = dot;
			curDots.put(curSound,f);
		}
		
	}
	
	public float[][] getMatrixFromFile(File f){
		DataInputStream dis = makeDIS(f);
		float[][] fl = new float[numWindows][30];
		for (int i=0;i<numWindows;i++){
			for (int j=0; j<30;j++){
				try {
					fl[i][j]=dis.readFloat();
				} catch (IOException e) {
					System.out.println("getMatrixFromFile Error in ReadFloat");
					e.printStackTrace();
				}
			}
		}
		return fl;
	}
		
	public void setupRecorder(){
		int minBufferSize = AudioRecord.getMinBufferSize(
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_IN_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT);
		bufferSize = minBufferSize;
	    buffer = new byte[minBufferSize];
		recorder = new AudioRecord(
	        		MediaRecorder.AudioSource.MIC,
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_IN_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT,
	        		minBufferSize);
		 
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
	
	public float dotProduct(float[] f1,float[] f2){
		int f1l=f1.length;
		int f2l=f2.length;
		if(f1l!=f2l){
			System.out.println("DOT PRODUCT ERROR VECTOR LENGTH MISMATCH");
			return 0;
		}
		else{
			float d = 0;
			for (int i=0;i<f1l;i++){
				d+=f1[i]*f2[i];
			}
			return d;
			
		}
		
	}
	
	public float sum(float[] f1){
		float s = 0;
		for (int i=0;i<f1.length;i++){
			s+=f1[i];
		}
		return s;
	}

	public float avg(float[] f1, int cc){
		int l = f1.length;
		float s = 0;
		for (int i=0;i<l;i++){
			s+=f1[i];
		}
		if(cc<=3){
			if(f1[1]==0){
				l-=1;
			}
			if(f1[1]==0){
				l-=1;
			}
		}
		return s/l;
	}
		
	public float stDev(float[] f, int cc){
		float avg = avg(f, cc);
		int l = f.length;
		float [] difs = new float[l];
		for (int i=0; i<l; i++){
			difs[i]=(f[i]-avg);
		}
		float std = (sum(difs))/l;
		return std;
		
	}
	
	public void printFeatureValues(float[][] f){
		System.out.println("CHECKING FEATURE VAULES");
		int i = 0;
		for (i=0;i<numWindows;i++){
			printMFCC(f[i],i);
		}
	}
		
	public void printMFCC(float[] f, int i){
		System.out.println("_______MFC#"+i+"_______");
		System.out.println(f[0]);
		System.out.println(f[1]);
		System.out.println(f[2]);
		System.out.println(f[3]);
		System.out.println(f[4]);
		System.out.println(f[5]);
		System.out.println(f[6]);
		System.out.println(f[7]);
		System.out.println(f[8]);
		System.out.println(f[9]);
		System.out.println(f[10]);
		System.out.println(f[11]);
		System.out.println(f[12]);
		System.out.println(f[13]);
		System.out.println(f[14]);
		System.out.println(f[15]);
		System.out.println(f[16]);
		System.out.println(f[17]);
		System.out.println(f[18]);
		System.out.println(f[19]);
		System.out.println(f[20]);
		System.out.println(f[21]);
		System.out.println(f[22]);
		System.out.println(f[23]);
		System.out.println(f[24]);
		System.out.println(f[25]);
		System.out.println(f[26]);
		System.out.println(f[27]);
		System.out.println(f[28]);
		System.out.println(f[29]);
	}
}


