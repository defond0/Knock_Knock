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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nav_action_bar, menu);
		menu.findItem(R.id.action_bar_training).setEnabled(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
	        case R.id.action_bar_home:
	        	toSplashPage();
	            return true;
	        case R.id.action_bar_settings:
	        	toSoundMenu();
	            return true;
		    case android.R.id.home:
		        NavUtils.navigateUpFromSameTask(this);
		        return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
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
