package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Notification_Screen extends Activity {
	
	private TextView notificationText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification__screen);
		Intent intent = getIntent();
		String notification = intent.getStringExtra(backGroundListener.EXTRA_MESSAGE);
		notificationText = (TextView) findViewById(R.id.notificationText);
		notificationText.setText(notification);
		startListening();
	}
	
	public void startListening(){
		Intent i = new Intent(this, backGroundListener.class);
		this.startService(i);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notification__screen, menu);
		return true;
	}
	
	public void toSplashPage(View view){
		//Method for button onClick, returns to SplashPage
		Intent i = new Intent(this, SplashPage.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
	    startActivity(i);
	}

}
