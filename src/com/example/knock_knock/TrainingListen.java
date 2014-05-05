package com.example.knock_knock;






import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.Oscilloscope;
import be.hogent.tarsos.dsp.Oscilloscope.OscilloscopeEventHandler;
import be.hogent.tarsos.dsp.mfcc.MFCC;
import be.hogent.tarsos.dsp.util.fft.FloatFFT;


public class TrainingListen extends Activity implements Handler.Callback, OscilloscopeEventHandler{
	//Draw Stuff
    private Paint myDrawPaint = new Paint();
    Canvas mainC;

	//Recording Stuff
	private int dSAMPLE_RATE = 44100;
	private int dBufferSize = 2048;
	private int overlap = 0;
	private be.hogent.tarsos.dsp.AudioFormat tarsosFormat;
	private DrawView mDrawView; 
	//Osci Listener
	private MicrophoneAudioDispatcher audioDispatcher;
	private boolean mIsRecording;
	
	
	
	private boolean isRecording;
	private Button recordButton;
	static final int SAMPLE_RATE = 16000;
	private byte[] buffer;
	private int bufferSize;
	private AudioRecord recorder;
	final static long recTime = 2500;
	final static String FV_PATH = "mfcc_val.xml";
	private float[][] featureValues;
	private int numVectors;
	final boolean debug = false;
	final Handler adamHandler = new Handler(this);
	private float MAX_CONVO;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_listen);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		isRecording = false; //might need to do something with app lifecycle
		
		mIsRecording = false;
		ViewGroup picLayout = (ViewGroup) findViewById(R.id.pictureLayout);
        mDrawView = new DrawView(this);
        mDrawView.setBackgroundColor(0x299ACC);
        picLayout.addView(mDrawView);
	}
	
    public void dListen() {
    	if (mIsRecording){
			if(audioDispatcher!= null){
				audioDispatcher.stop();
			}
	      	audioDispatcher = new MicrophoneAudioDispatcher(dSAMPLE_RATE, dBufferSize, overlap);
	      	audioDispatcher.addAudioProcessor(new Oscilloscope(this));
	    	(new Thread(audioDispatcher)).start();
    	}
    }

	@Override	
	public void handleEvent(float[] data, AudioEvent event) {
		mDrawView.paint(data, event);
	}
	
	
//    @Override
//    public void onPause() {
//    	super.onPause();
//    	audioDispatcher.stop();
//    }
//    @Override
//    public void onResume() {
//    	super.onPause();
//      	audioDispatcher = new MicrophoneAudioDispatcher(dSAMPLE_RATE, dBufferSize, overlap);
//      	audioDispatcher.addAudioProcessor(new Oscilloscope(this));
//    	(new Thread(audioDispatcher)).start();
//    }
	
	   class DrawView extends View{
	    	public float data[];

	        public DrawView(Context context) {
	            super(context);
	            myDrawPaint.setColor(Color.WHITE);
	            myDrawPaint.setAntiAlias(true);
	            myDrawPaint.setStrokeWidth(20);
	            myDrawPaint.setStyle(Paint.Style.STROKE);
	            myDrawPaint.setStrokeJoin(Paint.Join.ROUND);
	            myDrawPaint.setStrokeCap(Paint.Cap.ROUND);
	        }
	        
	        @Override
	        public void onDraw(Canvas canvas) {
	        	if (mIsRecording){
		    		System.out.println("onDraw.");
		            super.onDraw(canvas);
		
		            if(data != null){
						float width = getWidth();
						float height = getHeight();
						float halfHeight = height / 2;
						for(int i=0; i < data.length ; i+=4){
							 canvas.drawLine( data[i]* width, halfHeight - data[i+1]* height, 
									 data[i+2]*width,  halfHeight - data[i+3]*height, myDrawPaint);
						}
					}
	        	}
		    }
	        
			public void paint(float[] data, AudioEvent event){
				this.data = data;
				mDrawView.postInvalidate();
			}
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
			//recordButton.setText(getResources().getString(R.string.Done));
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					Message msg = new Message();
					msg.obj="Create Sound in 3";
					adamHandler.sendMessage(msg);
					}
			},0);
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					Message msg = new Message();
					msg.obj="Create Sound in 2";
					adamHandler.sendMessage(msg);
					}
			},1000);
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					Message msg = new Message();
					msg.obj="Create Sound in 1";
					adamHandler.sendMessage(msg);
					}
			},2000);
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					Message msg = new Message();
					msg.obj="Listening";
					adamHandler.sendMessage(msg);
				}
				
			},3000);
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					
					Message msg = new Message();
					msg.obj="Listening";
					adamHandler.sendMessage(msg);
					TrainingListen.this.listen();
					}
			},4000);
			adamHandler.postDelayed(new Runnable(){	
				public void run(){
					Message msg = new Message();
					msg.obj="Done";
					adamHandler.sendMessage(msg);
					
					
					//TROUBLE SECTION RIGHT HERE
					if (mIsRecording) {
						mIsRecording = false;
						audioDispatcher.stop();
					} else {
						mIsRecording = true;
						dListen();
					}	
				}
			},3000+recTime);
		}
		else {
			//recordButton.setText(getResources().getString(R.string.start));
			isRecording = false;
			Intent i = new Intent(this, TrainingFinal.class);
			i.putExtra("max",MAX_CONVO);
			audioDispatcher.stop();
		    startActivity(i);
		}
		

	}
		
	@Override
	public boolean handleMessage(Message arg0) {
		
		String msg = (String)arg0.obj;
		recordButton.setText(" "+ msg);
		return false;
	}		
	
	public void listen(){
	
		isRecording = true;
		Thread recordThread = new Thread(new Runnable() {
			@Override
			public void run() {
				//Set up Recorder 
				setupRecorder();	
				MAX_CONVO=0;
				//Set up mfcc extractor
				MFCC ap = new MFCC(buffer.length/2,SAMPLE_RATE);
				be.hogent.tarsos.dsp.AudioFormat tarForm = getFormat();
				AudioEvent ae = null;
				
				//Set up float array for storing the mfccs as they are calculated
				long secs = recTime/1000;
				long numSamples = secs*SAMPLE_RATE;
				numVectors = 64;
				featureValues=new float[numVectors][64];
				FloatFFT fft = new FloatFFT(32);
				
				//Record for recTime seconds 
				recorder.startRecording();
			
				//Timer for debugging
				long cur = System.currentTimeMillis();
				long start = cur;
				long rec = cur + recTime;
				
				if(debug){
					System.out.println(numVectors);
					System.out.println("Start ~ "+start);
				}
				//Recording Loop
				int i =0;
				
				float[] audioBufferFFT= new float[32];
				while (cur<rec){
					
					//read recorder
					long res = recorder.read(buffer, 0, buffer.length);
					
					//create audio event
					ae = new AudioEvent(tarForm, buffer.length);
					
					//Set over lap (this needs work)
					ae.setOverlap(buffer.length/2);
					ae.setFloatBufferWithByteBuffer(buffer);
					
					//use TarsosDsp MFCC to process signal
					ap.process(ae);
					
					//save 30 floats that MFCC returns, zero pad and forward fft
					audioBufferFFT=ap.getMFCC();
					for (int j = 0; j < 30; ++j) {
						featureValues[i][2*j] = audioBufferFFT[j];
						featureValues[i][2*j+1] = 0;
					}
					for (int j=29;j<32;j++){
						featureValues[i][2*j] = 0;
						featureValues[i][2*j+1] = 0;
					}
					fft.complexForward(featureValues[i]);
					//loop maintenance 
					i+=1;
					System.out.println(i);
					cur = System.currentTimeMillis();
				}
				
				
				
				
				System.out.println("Max CONVO "+MAX_CONVO);
				if(debug){
					long stop = System.currentTimeMillis();
					System.out.println("Stop ~ "+stop);
					System.out.println(stop-start);
				}
				recorder.stop();
				recorder.release();
				recorder = null;
				
				
				//for(int n = 0; n< 3*numVectors;n++){
				int M=numVectors;
				//System.out.println("n: "+n+ " convo: "+tmp);
				for (int m=0; m<2*M;m++){
					int fx = Math.abs(m%M);
					int gx = Math.abs((M-m)%M);
					float tmp=complexMultSumFFT(featureValues[fx],featureValues[gx],fft);
					System.out.println("convo: "+tmp);
						
				}
				
				
				
				
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
	
	public float complexMultSumFFT(float [] incoming, float[]template, FloatFFT fft){
		
		//NOTE TEMPLATE MUST BE ALREADY HAVE BEEN FORWARDFTT INTRAINING 
		
			float[] v = new float[64];
			float f =0;
			for (int j = 0; j < 32; ++j) {
				v[2*j]   = incoming[2*j]*template[2*j] - incoming[2*j+1]*template[2*j+1]; // real
				v[2*j+1] = incoming[2*j+1]*template[2*j] + incoming[2*j]*template[2*j+1]; // imaginary
			}
			fft.complexInverse(v, true);	
			for (int j = 0; j < 32; ++j) {
					f+=v[2*j];
			}
			
			return f;
	}
		
	
	///VARIOUS HELPER METHODS SOME FOR DEBUGGING SOME FOR ACTUAL STUFF 
	/// WARNING some are not threaded, but should only be called from within threads
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
		System.out.println(numVectors+", 64");
		for (int i=0;i<numVectors;i++){
			for(int j=0;j<64;j++){
				try {
					dos.writeFloat(featureValues[i][j]);
				} catch (IOException e) {
					System.out.println("FEATURE SAVE ERROR");
					e.printStackTrace();
				}
			}
		}
		//System.out.println(featureValues.);
		try {
			dos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public float sum(float[] f1){
		float s = 0;
		for (int i=0;i<f1.length;i++){
			s+=f1[i];
		}
		return s;
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
		float[][] fl = new float[numVectors][30];
		for (int i=0;i<numVectors;i++){
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
		for (i=0;i<numVectors;i++){
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



//////TEST CODE ///////
//				float tmp = 0;
//				float [][] matrix1 = new float[numVectors][64];
//				float [][] matrix2 = new float[numVectors][64];
//				
//				float[][] testMatrix = new float[3*numVectors][64];
//				for(int n = 0; n< 3*numVectors;n++){
//					for (int l = 0; l <numVectors;l++){
//						if(n>=0&&n<numVectors){
//							testMatrix[n][l]=1;
//						}
//						if(n>=numVectors&&n<=2*numVectors){
//							testMatrix[n][l]=featureValues[n%numVectors][l];
//						}
//						if(n>2*numVectors){
//							testMatrix[n][l]=2;
//						
//						}
//					}
//				}

//
//				
//				
//				
//				//////////////////////////////////////////////


}









