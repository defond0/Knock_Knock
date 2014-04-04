package com.example.knock_knock;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.IBinder;



public class backGroundListener extends Service implements OnsetHandler {
	//http://www.vogella.com/tutorials/AndroidServices/article.html
	
	final static int SAMPLE_RATE = 16000;
	private byte[] buffer;
	private int bufferSize;
	private be.hogent.tarsos.dsp.AudioFormat tarForm;
	private boolean rec;
	public final static String EXTRA_MESSAGE = "com.example.backGroundList.MESSAGE";
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    //TODO do something useful
		rec = true;
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
		
		//set up detector //
		final PercussionOnsetDetector pd = new PercussionOnsetDetector(SAMPLE_RATE, bufferSize/2, this, 25, 8);
		
		//start recording
		recorder.startRecording();
		tarForm= new be.hogent.tarsos.dsp.AudioFormat(SAMPLE_RATE,16,1,true,false);
		Thread listen = new Thread(new Runnable(){
			public void run(){	
				while (rec){
					int sig = recorder.read(buffer,0,bufferSize);
					AudioEvent ae = new AudioEvent(tarForm, sig);
					ae.setFloatBufferWithByteBuffer(buffer);
					pd.process(ae);
				}
				recorder.stop();
			}
		});
		listen.start();
	}

	@Override
	public void handleOnset(double time, double salience) {
		//only here to test out and make it run for claps
		rec = false;
		Intent i = new Intent(this, Notification_Screen.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		i.putExtra(EXTRA_MESSAGE, "Clap");
	    startActivity(i);
	    this.stopSelf();
	}
}
