package com.example.knock_knock;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SoundSettings extends Activity {
	
	
	private TableLayout soundTable;
	private Set<String> allSounds;
	private Set<String> checkedSounds;
	public static final String PREFS_NAME = "KnockKnockPrefs";
	private Set<String> deFault = new HashSet<String>();
	SharedPreferences prefs;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sound_settings);
		
		//Load previously set user preferences
		loadSoundSelectionPreferences();
		loadNotificationTypePreferences();
		loadAlertColorPreferences();		
		
	}
	
	private void loadSoundSelectionPreferences() {
		//Default sounds
		deFault.add("Clap");
		deFault.add("Whistle");
		deFault.add("Dummy0");
		deFault.add("Dummy1");
		
		//Get SharedPreferences and loud up sounds
		prefs = getSharedPreferences(PREFS_NAME, 0);
		allSounds = prefs.getStringSet("allSounds",deFault);
		checkedSounds = prefs.getStringSet("checkedSounds",new HashSet<String>());
		
		//Get Table
		soundTable = (TableLayout)findViewById(R.id.soundTable);
		
		//Load sound selection preferences
		Iterator<String> soundIter = allSounds.iterator();
		while(soundIter.hasNext()){
			final String curSound = soundIter.next();
			TableRow curRow = new TableRow(this);
			
			//Label for sound
			TextView text = new TextView(this);
			text.setText(curSound);
			text.setTextSize(20);
			
			//Toggle button for sound
			ToggleButton toggle = new ToggleButton(this);
			if(checkedSounds.contains(curSound)){
				toggle.setChecked(true);
			}
			else{
				toggle.setChecked(false);
			}
			toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked){
						checkedSounds.add(curSound);
					} else {
						checkedSounds.remove(curSound);
					}
				    SharedPreferences.Editor editor = prefs.edit();
				    editor.putStringSet("checkedSounds", checkedSounds);
				    editor.commit();
				}
				
			});

			//Align toggle button to right
			/*TableLayout.LayoutParams params = new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			params.weight = 1.0f;
			params.gravity=Gravity.RIGHT;
			toggle.setLayoutParams(params);*/
			
			//Add all components to view
			curRow.addView(text);
			curRow.addView(toggle);
			soundTable.addView(curRow);
		}
	}
	
	private void loadNotificationTypePreferences() {
		//Iterate through each type of preference, and check the box if
		//the user previously turned it on
		//By default, all notification types are turned on
		Boolean alert = prefs.getBoolean("notificationTypeAlert", true);
		if (alert) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypeAlert);
			cb.setChecked(true);
		}
		Boolean push = prefs.getBoolean("notificationTypePush", true);
		if (push) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypePush);
			cb.setChecked(true);
		}
		Boolean vibrate = prefs.getBoolean("notificationTypeVibrate", true);
		if (vibrate) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypeVibrate);
			cb.setChecked(true);
		}
	}
	
	private void loadAlertColorPreferences() {
		//Set the radio button to the correct color that was previously chosen
		//Default: red
		String alertColor = prefs.getString("alertColor", "Red");
		RadioButton rb = null;
		if (alertColor.equals("Red")) {
			rb = (RadioButton) findViewById(R.id.alertColorRed);
		} else if (alertColor.equals("Blue")) {
			rb = (RadioButton) findViewById(R.id.alertColorBlue);
		} else if (alertColor.equals("Green")) {
			rb = (RadioButton) findViewById(R.id.alertColorGreen);
		} else if (alertColor.equals("Purple")) {
			rb = (RadioButton) findViewById(R.id.alertColorPurple);
		}
		rb.toggle();
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
	
	public void onAlertColorSelected(View view) {
		//Change the stored color alert preference to the matching radio button
		SharedPreferences.Editor editor = prefs.edit();
		switch(view.getId()) {
        	case R.id.alertColorRed:
        		editor.putString("alertColor", "Red");
        		editor.commit();
        		break;
        	case R.id.alertColorBlue:
        		editor.putString("alertColor", "Blue");
        		editor.commit();
        		break;
        	case R.id.alertColorGreen:
        		editor.putString("alertColor", "Green");
        		editor.commit();
        		break;
        	case R.id.alertColorPurple:
        		editor.putString("alertColor", "Purple");
        		editor.commit();
        		break;
		}
	}
	
	public void onNotificationSelected(View view) {
		//Changed the stored notification preferences to match the checkboxes
		boolean checked = ((CheckBox) view).isChecked();
		SharedPreferences.Editor editor = prefs.edit();
		switch(view.getId()) {
			case R.id.notificationTypeAlert:
				if (checked) {
					editor.putBoolean("notificationTypeAlert", true);
				} else {
					editor.putBoolean("notificationTypeAlert", false);
				}
				editor.commit();
				break;
			case R.id.notificationTypePush:
				if (checked) {
					editor.putBoolean("notificationTypePush", true);
				} else {
					editor.putBoolean("notificationTypePush", false);
				}
				editor.commit();
				break;
			case R.id.notificationTypeVibrate:
				if (checked) {
					editor.putBoolean("notificationTypeVibrate", true);
				} else {
					editor.putBoolean("notificationTypeVibrate", false);
				}
				editor.commit();
				break;
			}
	}

}

