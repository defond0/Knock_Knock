package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class SplashPage extends Activity {
	
	private Button on, soundSettings, training;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_page);
		
		//initialize buttons (jic)
		on = (Button) findViewById(R.id.onButton);
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
		Intent intent = new Intent(this, TrainingMenu.class);
	    startActivity(intent);
	}
	
	public void toSoundMenu(View view){
		//Method for button onClick, returns to SoundSettingMenu
		Intent intent = new Intent(this, SoundSettings.class);
	    startActivity(intent);
	}

}
