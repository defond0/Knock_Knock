package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class TrainingListen extends Activity {
	
	private boolean isRecording;
	private Button recordButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_listen);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		isRecording = false; //might need to do something with app lifecycle
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.training_listen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // This is broken, check activity stack?
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public void recordHandler(View view){
		recordButton = (Button) findViewById(R.id.recordButton);
		if (!isRecording) {
			recordButton.setText(getResources().getString(R.string.Done));
			isRecording = true;
		} else {
			//recordButton.setText(getResources().getString(R.string.start));
			isRecording = false;
			Intent i = new Intent(this, TrainingFinal.class);
		    startActivity(i);
		}
	}

}
