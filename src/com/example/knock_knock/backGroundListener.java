package com.example.knock_knock;

import java.util.HashSet;
import java.util.Set;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;



public class backGroundListener extends Service implements OnsetHandler, PitchDetectionHandler {
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
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //TODO do something useful
		rec = true;
		//Get SharedPreferences and loud up sounds
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		checkedSounds = PreferenceStorage.getAllCheckedSounds(prefs);
		notification = "";
		listen();
		return super.onStartCommand(intent,flags,startId);
	  }
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	private void listen(){	
		//set up recorder
		bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);					
		buffer = new byte[bufferSize];
		final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
		
		//set up "clap" detector //
		final PercussionOnsetDetector pd = new PercussionOnsetDetector(SAMPLE_RATE, bufferSize/2, this, 16, 8);
		
		//set up "all" detector //
		final PitchProcessor pp = new PitchProcessor( PitchProcessor.PitchEstimationAlgorithm.AMDF,SAMPLE_RATE,bufferSize,this);
		
		//start recording
		recorder.startRecording();
		tarForm= new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE,16,1,true,false);
		Thread listen = new Thread(new Runnable(){
			public void run(){	
				while (rec){
					System.out.println(System.currentTimeMillis());
					int sig = recorder.read(buffer,0,bufferSize);
					AudioEvent ae = new AudioEvent(tarForm, sig);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//WOZ for clap
					if(checkedSounds.contains("Clap")){
						pd.process(ae);
					}
					//Wos for all
					if(checkedSounds.contains("Whistle")){
						pp.process(ae);
					}
				}
				recorder.stop();
			}
		});
		listen.start();
	}
	

	@Override
	public void handleOnset(double time, double salience) {
		//only here to test out and make it run for claps
		notification = "Clap";
		rec = false;
		Intent i = new Intent(this, Notification_Screen.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.putExtra(EXTRA_MESSAGE, notification);
		//fix once we can detect different sounds
		i.putExtra(SOUND_NAME, notification);
	    startActivity(i);
	    this.stopSelf();
	}

	@Override
	public void handlePitch(PitchDetectionResult pitchDetectionResult,
			AudioEvent audioEvent) {
		//only here to test out and make it run for whistle
		if(pitchDetectionResult.isPitched()){
			notification = "Whistle";
			rec = false;
			Intent i = new Intent(this, Notification_Screen.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			i.putExtra(EXTRA_MESSAGE, notification);
			//fix when we can detect different sounds
			i.putExtra(SOUND_NAME, notification);
		    startActivity(i);
		    this.stopSelf();
		}
		
	}
}
