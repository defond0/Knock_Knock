package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class SplashPage extends Activity {
	
	private Button on, soundSettings, training;
	private static boolean listening;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_page);
		setListening(false);
		
		//initialize buttons (jic)
		on = (Button) findViewById(R.id.onButton);
		on.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View arg0) {
				if(isListening()){
					on.setText(getResources().getString(R.string.ON));
					setListening(false);
					//End Backgound Service to listen
				}
				else{
					on.setText(getResources().getString(R.string.OFF));
					//Start Background Service to listen
					startListenting();
				}
				
			}
			
		});
		
		soundSettings = (Button) findViewById(R.id.soundSettingsButton);
		training = (Button) findViewById(R.id.trainingButton);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_page, menu);
		return true;
	}
	
	
	public void toTrainMenu(View view){
		//Method for button onClick, returns to TrainingMenu
		Intent i = new Intent(this, TrainingMenu.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}
	
	public void toSoundMenu(View view){
		//Method for button onClick, returns to SoundSettingMenu
		Intent i = new Intent(this, SoundSettings.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}
	
	public void startListenting(){
		setListening(true);
		Intent i = new Intent(this, backGroundListener.class);
		this.startService(i);
	}
	public void stopListenting(){
		setListening(false);
		Intent i = new Intent(this, backGroundListener.class);
		this.stopService(i);
	}

	public static boolean isListening() {
		return listening;
	}

	public static void setListening(boolean listening) {
		SplashPage.listening = listening;
	}

}
