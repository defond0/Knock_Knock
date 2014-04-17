package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class SplashPage extends Activity {

	private Button soundSettings, training;
	private ToggleButton onOff;
	private static boolean listening;
	public static final String PREFS_NAME = "KnockKnockPrefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_page);

		// checks prefs to alert us if we are listening
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		setListening(PreferenceStorage.getIsRec(prefs));

		// initialize buttons (jic)
		onOff = (ToggleButton) findViewById(R.id.splashOnOff);
		onOff.setChecked(isListening());
		onOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onOff.isChecked()) {
					startListening();
				} else {
					stopListenting();
				}
			}
		});
		soundSettings = (Button) findViewById(R.id.soundSettingsButton);
		training = (Button) findViewById(R.id.trainingButton);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		setListening(PreferenceStorage.getIsRec(prefs));
		if (isListening()) {
			onOff.setChecked(true);
		} else {
			onOff.setChecked(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();               
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_page, menu);
		return true;
	}

	public void toTrainMenu(View view) {
		// Method for button onClick, returns to TrainingMenu
		Intent i = new Intent(this, TrainingMenu.class);
		startActivity(i);
	}

	public void toSoundMenu(View view) {
		// Method for button onClick, returns to SoundSettingMenu
		Intent i = new Intent(this, SoundSettings.class);
		startActivity(i);
	}

	public void startListening() {
		setListening(true);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		PreferenceStorage.setIsRec(prefs,isListening());
		Intent i = new Intent(this, backGroundListener.class);
		this.startService(i);
	}

	public void stopListenting() {
		setListening(false);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		PreferenceStorage.setIsRec(prefs,isListening());
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
