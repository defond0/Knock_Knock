package com.example.knock_knock;

import jAudioFeatureExtractor.DataModel;
import jAudioFeatureExtractor.ACE.DataTypes.Batch;
import jAudioFeatureExtractor.ACE.DataTypes.FeatureDefinition;
import jAudioFeatureExtractor.AudioFeatures.MFCC;
import jAudioFeatureExtractor.DataTypes.RecordingInfo;
import jAudioFeatureExtractor.jAudioTools.*;
import jAudioFeatureExtractor.jAudioTools.AudioSamples;

import javax.sound.sampled.AudioInputStream; 


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
//import jAudioFeatureExtractor.ACE.XMLDocumentParser;
//import org.apache.xerces.parsers.SAXParser;

public class TrainingListen extends Activity {
	
	private boolean isRecording;
	private Button recordButton;
	static final int SAMPLE_RATE = 16000;
	private AudioTrack audioTrack;
	private byte[] buffer;
	private AudioRecord recorder;
	final static long recTime = 5000;
	final static String SUB_DIR = "knock_Knock_templates";
	final static String CUR_PCM ="curTraining.pcm";
	final static String FK_PATH ="mfcc_result.xml";
	final static String FV_PATH = "curTrainingTempalate.xml";
	final static String FEATURES_PATH = "features.xml";

	
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
			isRecording = true;
			listen();
		} else {
			//recordButton.setText(getResources().getString(R.string.start));
			isRecording = false;
			Intent i = new Intent(this, TrainingFinal.class);
		    startActivity(i);
		}
	}
	
	public void listen(){
		 int minBufferSize = AudioRecord.getMinBufferSize(
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_IN_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT);
	     buffer = new byte[minBufferSize];
		 audioTrack = new AudioTrack(
	        		AudioManager.STREAM_MUSIC,
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_OUT_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT,
	        		minBufferSize,
	        		AudioTrack.MODE_STREAM);
		 recorder = new AudioRecord(
	        		MediaRecorder.AudioSource.MIC,
	        		SAMPLE_RATE,
	        		AudioFormat.CHANNEL_IN_MONO,
	        		AudioFormat.ENCODING_PCM_16BIT,
	        		minBufferSize);
		
		 recorder.startRecording();
		Thread recordThread = new Thread(new Runnable() {
			@Override
			public void run() {
				//Record for 5 seconds TODO ADD FEATURE EXTRACTOR AND SAVE MATRIX of col feature vectors at 
				//time windows
				
				long cur = System.currentTimeMillis();
				long start = cur;
				System.out.println("Start ~ "+start);
				long rec = cur + recTime;
				File dir = getDir(SUB_DIR,Context.MODE_PRIVATE);
				if(!dir.exists()){
					dir.mkdir();
				}
				File sample = new File(dir.getAbsolutePath() + File.separator + CUR_PCM);
				try {
					sample.createNewFile();
				} catch (IOException e1) {
					System.out.println("cant create soundfile");
					e1.printStackTrace();
				}
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(sample);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String path = "";
				while (cur<rec){
					int res = recorder.read(buffer, 0, buffer.length);
					//audioTrack.write(buffer, 0, res);
					try {
						fos.write(buffer,0,buffer.length);
					} catch (FileNotFoundException e) {
						System.out.println("Error in Creating OutputStream in TrainListening");
						e.printStackTrace();
					} catch (IOException e) {
						System.out.println("Error in Writing OutputStream in TrainListening");
						e.printStackTrace();
					}
					cur = System.currentTimeMillis();
				}
				isRecording = false;
				long stop = System.currentTimeMillis();
				System.out.println("Stop ~ "+stop);
				System.out.println(stop-start);
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recorder.stop();
				recorder.release();
				recorder = null;
			
				System.out.println("+++++++++++++Begining to Create and Execute Batch+++++++++++");
				
				File[] samplePath = {sample};
				File fk = new File(dir,FK_PATH);
				File fv = new File(dir,FV_PATH);
				try {
					fk.createNewFile();
					fv.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
					 
				Batch tMB = new Batch();
				MFCC mfcc = new MFCC();
					
					
				FeatureDefinition mfccDef=mfcc.getFeatureDefinition();
	
				HashMap<String, Boolean>ActiveFeatures = new HashMap<String,Boolean>();
				ActiveFeatures.put(mfccDef.name,true);
				
				HashMap<String,String[]>FeatureAttributes = new HashMap<String,String[]>();
				FeatureAttributes.put(mfccDef.name,new String[]{"5"});
					
					
				tMB.setFeatures(ActiveFeatures,FeatureAttributes);
				//tMB.setSettings(512,.5,SAMPLE_RATE,true,true,false,13);
				tMB.setDestination(dir.getPath()+"/"+FK_PATH,dir.getPath()+"/"+FV_PATH);
				
				
				AudioSamples aS = null;
				try {
					aS = new AudioSamples(sample,CUR_PCM, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("DING");
				RecordingInfo[] sampleFileInfo = new RecordingInfo[5];
				sampleFileInfo[0]= new RecordingInfo("0",sample.getPath(),aS,true);
				RecordingInfo[][] recInfo = new RecordingInfo[5][5];
				recInfo[0]=sampleFileInfo;
				int[] WindowSize = new int[]{4096};
				double[] OverLap = new double[]{.5};
				double[] Sample_Rate = new double[]{SAMPLE_RATE};
				boolean[] normalize = new boolean[]{true};
				boolean[] eachWindow = new boolean[]{true};
				boolean[] overall = new boolean[]{false};
				tMB.applySettings(recInfo,WindowSize, OverLap,
						Sample_Rate,
						normalize, eachWindow,
						overall, 
						new String[]{fk.getPath()},
						new String[]{fv.getPath()},
						new int[]{0});
				//tMB.outputXML();
				try {
					tMB.setRecordings(samplePath);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					tMB.execute();
				} catch (Exception e) {
					System.out.println("Execution Error");
					e.printStackTrace();
				}
				
			}
		});
		recordThread.run();	
		
	}
	
//	public String featurePath(){
//		
//		InputStream inStream=getResources().openRawResource(R.raw.features);
//		BufferedReader is = new BufferedReader(new InputStreamReader(inStream));
//		File features=null;
//		try {
//			File dir = getDir(SUB_DIR,Context.MODE_PRIVATE);
//			if(!dir.exists()){
//				dir.mkdir();
//			}
//			features = new File(dir.getAbsolutePath() + File.separator + FEATURES_PATH);
//			try {
//				features.createNewFile();
//			} catch (IOException e1) {
//				System.out.println("cant create feature.xml file");
//				e1.printStackTrace();
//			}
//			final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(features));
//			String line = null;
//			while((line=is.readLine())!=null){
//				bos.write(line.getBytes());
//			}
//			is.close();
//			bos.close();
//			
//		} catch (FileNotFoundException e) {
//			System.out.println("File not found Error");
//			e.printStackTrace();
//		} catch (IOException e) {
//			System.out.println("Read Error in loop");
//			e.printStackTrace();
//		}
//		System.out.println(features.exists());
//		return features.getPath();
//	}
	
//	public void printFile(String path){
//		final String p = path;
//		Thread printThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				File f = new File(p);
//				BufferedReader is = null;
//				try {
//					is = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				String line = null;
//				try {
//					while((line=is.readLine())!=null){
//						System.out.println(line);
//					}
//					is.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				
//			
//			}
//	
//		});
//		
//	printThread.run();
//
//	}
}	


