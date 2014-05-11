package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TrainingMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	
	}
	
	public void toTrainListen(View view){
		Intent i = new Intent(this, TrainingListen.class);
		startActivity(i);
	}
	
	public void toSoundMenu(){
		Intent i = new Intent(this, SoundSettings.class);
	    startActivity(i);
	}
	
	public void toSplashPage(){
		//Method for button onClick, returns to SplashPage
		Intent i = new Intent(this, SplashPage.class);
	    startActivity(i);
	}

}
