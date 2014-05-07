package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Notification_Screen extends Activity {
	
	private TextView notificationText;
	private SharedPreferences prefs ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification__screen);
		//Display type of sound that was detected
		Intent intent = getIntent();
		String notification="";
		Bundle b = getIntent().getExtras();
		if(b.getString("notification")!=null){
			notification = b.getString("notification");
		}
		notificationText = (TextView) findViewById(R.id.notificationText);
		notificationText.setText(notification);
		//Change background color
		prefs = getSharedPreferences(SoundSettings.PREFS_NAME, 0);
		int color = PreferenceStorage.getAlertColor(prefs,notification, getResources());
		RelativeLayout container = (RelativeLayout) findViewById(R.id.notificationScreenContainer);
		container.setBackgroundColor(color);
		//Default listening
		startListening();
	}
	
	public void startListening(){
		Intent i = new Intent(this, backGroundListener.class);
		this.startService(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.notification__screen, menu);
		return true;
	}
	
	public void toSplashPage(View view){
		//Method for button onClick, returns to SplashPage
		Intent i = new Intent(this, SplashPage.class);
	    startActivity(i);
	}

}
