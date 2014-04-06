package com.example.knock_knock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

public class NotificationSettings extends Activity {
	
	public static final String PREFS_NAME = "KnockKnockPrefs";
	private SharedPreferences prefs;
	private String soundName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_settings);
		
		//Set custom title
		Intent i = getIntent();
		soundName = (String) i.getExtras().get("sound");
		TextView text = (TextView) findViewById(R.id.notificationSettingsTitle);
		text.setText(soundName + " Preferences");
		
		prefs = getSharedPreferences(PREFS_NAME, 0);
		
		loadNotificationTypePreferences();
		loadAlertColorPreferences();
	}
	
	private void loadNotificationTypePreferences() {
		//Iterate through each type of preference, and check the box if
		//the user previously turned it on
		//By default, all notification types are turned on
		Boolean alert = PreferenceStorage.isAlertNotifOn(prefs, soundName);
		if (alert) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypeAlert);
			cb.setChecked(true);
		}
		Boolean push = PreferenceStorage.isPushNotifOn(prefs, soundName);
		if (push) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypePush);
			cb.setChecked(true);
		}
		Boolean vibrate = PreferenceStorage.isVibrateNotifOn(prefs, soundName);
		if (vibrate) {
			CheckBox cb = (CheckBox) findViewById(R.id.notificationTypeVibrate);
			cb.setChecked(true);
		}
	}
	
	private void loadAlertColorPreferences() {
		//Set the radio button to the correct color that was previously chosen
		//Default: red
		String alertColor = PreferenceStorage.getAlertColor(prefs, soundName);
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
	
	public void onAlertColorSelected(View view) {
		//Change the stored color alert preference to the matching radio button
		switch(view.getId()) {
        	case R.id.alertColorRed:
        		PreferenceStorage.setColor(prefs, soundName, "Red");
        		break;
        	case R.id.alertColorBlue:
        		PreferenceStorage.setColor(prefs, soundName, "Blue");
        		break;
        	case R.id.alertColorGreen:
        		PreferenceStorage.setColor(prefs, soundName, "Green");
        		break;
        	case R.id.alertColorPurple:
        		PreferenceStorage.setColor(prefs, soundName, "Purple");
        		break;
		}
	}
	
	public void onNotificationSelected(View view) {
		//Changed the stored notification preferences to match the checkboxes
		boolean checked = ((CheckBox) view).isChecked();
		switch(view.getId()) {
			case R.id.notificationTypeAlert:
				if (checked) {
					PreferenceStorage.setAlertNotif(prefs, soundName, true);
				} else {
					PreferenceStorage.setAlertNotif(prefs, soundName, false);
				}
				break;
			case R.id.notificationTypePush:
				if (checked) {
					PreferenceStorage.setPushNotif(prefs, soundName, true);
				} else {
					PreferenceStorage.setPushNotif(prefs, soundName, false);
				}
				break;
			case R.id.notificationTypeVibrate:
				if (checked) {
					PreferenceStorage.setVibrate(prefs, soundName, true);
				} else {
					PreferenceStorage.setVibrate(prefs, soundName, false);
				}
				break;
			}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.nav_action_bar, menu);
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
	        case R.id.action_bar_settings:
	        	toSoundMenu();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
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
	
	public void toSoundMenu(){
		Intent i = new Intent(this, SoundSettings.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}

}
