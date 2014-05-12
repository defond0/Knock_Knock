package com.example.knock_knock;

import java.util.Iterator;
import java.util.Set;

import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class SplashPage extends Activity {

	private ToggleButton onOff;
	private Set<String> checkedSounds;
	public static final String PREFS_NAME = "KnockKnockPrefs";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_page);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		
		onOff = (ToggleButton) findViewById(R.id.splashOnOff);
		onOff.setChecked(PreferenceStorage.getON_OFF(prefs));
		onOff.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				if (onOff.isChecked()) {
					System.out.println("Startlistening");
					startListening();
				} else {
					System.out.println("Stoplistening");
					stopListenting();
				}
			}
		});
	}

	public void toTrainMenu(View view) {
		// Method for button onClick, returns to TrainingMenu
		Intent i = new Intent(this, TrainingMenu.class);
		startActivity(i);
		this.onStop();
	}

	public void toSoundMenu(View view) {
		// Method for button onClick, returns to SoundSettingMenu
		Intent i = new Intent(this, SoundSettings.class);
		startActivity(i);
		this.onStop();
	}
	
	public final static String EXTRA_MESSAGE = "com.example.backGroundList.MESSAGE";
	public final static String SOUND_NAME = "com.example.backGroundList.SOUNDNAME";
	
	public void openNotification(View view) {
		String notification = "Clap";
		Intent i = new Intent(this, Notification_Screen.class);
		i.putExtra(EXTRA_MESSAGE, notification);
		//fix once we can detect different sounds
		i.putExtra(SOUND_NAME, notification);
	    startActivity(i);
	}

	public void startListening() {
		final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		PreferenceStorage.setON_OFF(prefs,true);
	
				checkedSounds = PreferenceStorage.getAllCheckedSounds(prefs);
				Iterator<String> soundIter = checkedSounds.iterator();
				while(soundIter.hasNext()){
					String curSound = soundIter.next();
					Intent i = new Intent(SplashPage.this, backGroundListener.class);
					i.putExtra("sound",curSound);
					System.out.println(curSound);
					SplashPage.this.startService(i);  ///SET DATA NOT OBJ (DIF PROCESS)
				}
		
	}

	public void stopListenting() {
		Thread stopListen = new Thread(new Runnable(){
				@Override
				public void run(){
					SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
					PreferenceStorage.setON_OFF(prefs,false);
					SplashPage.this.stopService(new Intent(SplashPage.this, backGroundListener.class));
				}
			});
		stopListen.run();
	}

	
}
