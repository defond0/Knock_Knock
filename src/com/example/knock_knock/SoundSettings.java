package com.example.knock_knock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class SoundSettings extends Activity {
	
	
	private TableLayout soundTable;
	private Set<String> allSounds;
	private Set<String> checkedSounds;
	public static final String PREFS_NAME = "KnockKnockPrefs";
	private Set<String> deFault = new HashSet<String>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_settings);
		
		//Default sounds
		deFault.add("Clap");
		deFault.add("Whistle");
		deFault.add("Dummy0");
		deFault.add("Dummy1");
		
		//Get SharedPreferences and loud up sounds
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		allSounds = prefs.getStringSet("allSounds",deFault);
		checkedSounds = prefs.getStringSet("checkedSounds",new HashSet<String>());
		
		//Get Table
		soundTable = (TableLayout)findViewById(R.id.soundTable);
		
		//Load
		Iterator<String> soundIter = allSounds.iterator();
		while(soundIter.hasNext()){
			String curSound = soundIter.next();
			TableRow curRow = new TableRow(this);
			CheckBox curBox = new CheckBox(this);
			curBox.setText(curSound);
			if(checkedSounds.contains(curSound)){
				curBox.setChecked(true);
			}
			else{
				curBox.setChecked(false);
			}
			curBox.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View box) {
					if(((CheckBox)box).isChecked()){
						checkedSounds.add(((CheckBox)box).getText().toString());
					}
					else
						checkedSounds.remove(((CheckBox)box).getText().toString());		
				}
				
			});
			curRow.addView(curBox);
			soundTable.addView(curRow);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nav_action_bar, menu);
		menu.findItem(R.id.action_bar_settings).setEnabled(false);
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
	        case R.id.action_bar_training:
	        	toTrainMenu();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	
	@Override
    protected void onStop(){
       super.onStop();
      // We need an Editor object to make preference changes.
      // All objects are from android.context.Context
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      //editor.putStringSet("allSounds", allSounds);
      editor.putStringSet("checkedSounds", checkedSounds);
      editor.commit();
	}
	
	public void toTrainMenu(){
		//Method for button onClick, returns to TrainingMenu
		Intent i = new Intent(this, TrainingMenu.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}
	
	public void toSplashPage(){
		//Method for button onClick, returns to SplashPage
		Intent i = new Intent(this, SplashPage.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}

}

