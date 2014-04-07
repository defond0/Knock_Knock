package com.example.knock_knock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class TrainingFinal extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_final);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.training_final, menu);
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
	
	public void saveSound(View view){
		EditText newSoundLabel = (EditText) findViewById(R.id.newSoundLabel);
		String label = newSoundLabel.getText().toString();
		PreferenceStorage.addSound(getSharedPreferences(SoundSettings.PREFS_NAME, 0), label);
		
		Intent i = new Intent(this, SoundSettings.class);
		startActivity(i);
	}

}
