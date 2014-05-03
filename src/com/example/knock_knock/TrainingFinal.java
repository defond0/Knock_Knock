package com.example.knock_knock;

import java.io.File;
import java.io.IOException;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class TrainingFinal extends Activity {
	
	//Location of the newly created mfcc feature matrix
	final static String FV_PATH = "mfcc_val.xml";
	private float maxConvo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_final);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle b = getIntent().getExtras();
		if(b.getFloat("max")!=0){
			maxConvo=b.getFloat("max");
		}
		else{
			maxConvo=Float.NEGATIVE_INFINITY;
		}
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
		SharedPreferences prefs=getSharedPreferences(SoundSettings.PREFS_NAME, 0);
		PreferenceStorage.addSound(prefs, label);
		PreferenceStorage.setAverageConvo(prefs,label,maxConvo);
		createNewTemplateFile(label);
		Intent i = new Intent(this, SoundSettings.class);
		startActivity(i);
	}
	
	public void createNewTemplateFile(String path){
		final String p =path+".xml";
		Thread renameThread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				File reName = makeNewFile(p);
				File cur = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).
						getAbsolutePath()+File.separator+FV_PATH);
				cur.renameTo(reName);
			}
			
		});
		renameThread.run();
	}
	
	public File makeNewFile(String path){
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath());
		if(!dir.exists()){
			dir.mkdir();
		}
		File f = new File(dir.getAbsolutePath() + File.separator + path);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			System.out.println("Cant create new file: "+path);
			e1.printStackTrace();
		}
		return f;
		
	}

}
