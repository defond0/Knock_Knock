package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class SoundSettings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sound_settings, menu);
		return true;
	}
	
	public void toTrainMenu(View view){
		//Method for button onClick, returns to TrainingMenu
		Intent intent = new Intent(this, TrainingMenu.class);
	    startActivity(intent);
	}
	
	public void toSplashPage(View view){
		//Method for button onClick, returns to SplashPage
		Intent intent = new Intent(this, SplashPage.class);
	    startActivity(intent);
	}

}

