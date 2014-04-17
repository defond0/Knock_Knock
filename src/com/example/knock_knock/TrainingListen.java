package com.example.knock_knock;






import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.mfcc.MFCC;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class TrainingListen extends Activity {
	
	private boolean isRecording;
	private Button recordButton;
	static final int SAMPLE_RATE = 16000;
	private AudioTrack audioTrack;
	private byte[] buffer;
	private int bufferSize;
	private AudioRecord recorder;
	final static long recTime = 5000;
	final static String FV_PATH = "mfcc_val.xml";
	private float[][] featureValues;
	private int numWindows;
	final boolean debug = false;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_listen);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		isRecording = false; //might need to do something with app lifecycle
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.training_listen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // This is broken, check activity stack?
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public void recordHandler(View view){
		recordButton = (Button) findViewById(R.id.recordButton);
		if (!isRecording) {
			recordButton.setText(getResources().getString(R.string.Done));
			listen();
		} else {
			//recordButton.setText(getResources().getString(R.string.start));
			isRecording = false;
			Intent i = new Intent(this, TrainingFinal.class);
		    startActivity(i);
		}
	}
	
	public void listen(){
		isRecording = true;
		Thread recordThread = new Thread(new Runnable() {
			@Override
			public void run() {
				//Set up Recorder 
				setupRecorder();	
				
				//Set up mfcc extractor
				MFCC ap = new MFCC(buffer.length/2,SAMPLE_RATE);
				be.hogent.tarsos.dsp.AudioFormat tarForm = getFormat();
				AudioEvent ae = null;
				
				//Set up float array for storing the mfccs as they are calculated
				long secs = recTime/1000;
				long numSamples = secs*SAMPLE_RATE;
				int nS = (int)(long)numSamples;
				numWindows =(((nS/bufferSize)+1)*2);
				featureValues=new float[numWindows][30];
				
				
				//Record for recTime seconds and save pcm file
				recorder.startRecording();
			
				//Timer for debugging
				long cur = System.currentTimeMillis();
				long start = cur;
				long rec = cur + recTime;
				
				if(debug){
					System.out.println(numWindows);
					System.out.println("Start ~ "+start);
				}
				//Recording Loop
				int i =0;
				while (cur<rec){
					
					//read recorder
					long res = recorder.read(buffer, 0, buffer.length);
					
					//create audio event
					ae = new AudioEvent(tarForm, res);
					
					//Set over lap (this needs work)
					ae.setOverlap(buffer.length/4);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//use TarsosDsp MFCC to process signal
					ap.process(ae);
					
					//save 30 floats that MFCC returns
					featureValues[i]=ap.getMFCC();
					
					//loop maintenance 
					i+=1;
					cur = System.currentTimeMillis();
				}
				//Kill Recording
				
				if(debug){
					long stop = System.currentTimeMillis();
					System.out.println("Stop ~ "+stop);
					System.out.println(stop-start);
				}
				recorder.stop();
				recorder.release();
				recorder = null;
				
				//Save Features
				File FV = makeNewFile(FV_PATH);
				saveFeatureValues(FV);
				if(debug){
					checkFile(FV);
				}
			}
			});
			recordThread.run();	
			
	}
	
	///VARIOUS HELPER METHODS SOME FOR DEBUGGING SOME FOR ACTUAL STUFF 
	/// some are not threaded, but should only be called from within threads
	public be.hogent.tarsos.dsp.AudioFormat getFormat(){
		be.hogent.tarsos.dsp.AudioFormat aF = new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE,16,1,true,false);
		return aF;
		
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
	
	public void saveFeatureValues(File FV){
		//Set up file Stream
		DataOutputStream dos = makeDOS(FV); 
		
		//Write the Values from featureValues into data output stream dos
		for (int i=0;i<numWindows;i++){
			for(int j=0;j<30;j++){
				try {
					dos.writeFloat(featureValues[i][j]);
				} catch (IOException e) {
					System.out.println("FEATURE SAVE ERROR");
					e.printStackTrace();
				}
			}
		}
	}
	
	public void printFile(String path){
				File f = new File(path);
				BufferedReader is = null;
				try {
					is = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String line = null;
				try {
					while((line=is.readLine())!=null){
						System.out.println(line);
					}
					is.close();
    			} catch (IOException e) {
					e.printStackTrace();
				}
				
			
			}

	
	public File makeNewFile(String path){
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
		if(!dir.exists()){
			dir.mkdir();
		}
		File f = new File(dir.getAbsolutePath() + File.separator + path);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			System.out.println("cant create new file: "+path);
			e1.printStackTrace();
		}
		return f;
		
	}
	
	
	public DataOutputStream makeDOS(File f){
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(f));
		} catch (FileNotFoundException e1) {
			System.out.println("DOS error with " + f.getPath());
			e1.printStackTrace();
		}
		return dos;
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
	
	public void checkFile(File f){
		System.out.println("FILE CHECK "+f.getPath());
		DataInputStream dis = makeDIS(f);
		float[][] fl = new float[numWindows][30];
		for (int i=0;i<numWindows;i++){
			for (int j=0; j<30;j++){
				try {
					fl[i][j]=dis.readFloat();
				} catch (IOException e) {
					System.out.println("CheckFile Error in ReadFloat");
					e.printStackTrace();
				}
			}
		}
		printFeatureValues(fl);
	}

	public void printFeatureValues(float[][] f){
		System.out.println("CHECKING FEATURE VAULES");
		int i = 0;
		for (i=0;i<numWindows;i++){
			printMFCC(f[i],i);
		}
	}
	
	
	public void printMFCC(float[] f, int i){
		//System.out.println(ae.getSamplesProcessed());
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









