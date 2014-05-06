package com.example.knock_knock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.content.SharedPreferences;
import android.content.res.Resources;

public class PreferenceStorage {
	
	//Storage for sound preferences
	SharedPreferences prefs;
	
	//Keys for storing preferences
	public static final String ON_OFF = "on_off";
	public static final String ALL_SOUNDS = "allSounds";
	public static final String ON = "_on";
	public static final String ALERT_COLOR = "_alertcolor";
	public static final String PUSH_NOTIF = "_push";
	public static final String ALERT_NOTIF = "_alert";
	public static final String VIBRATE_NOTIF = "_vibrate";
	public static final String DETECTED = "_detected";
	public static final String MAX_CONVOLUTION = "_maxConvo";
	public static final String CUR_CONVOLUTION = "_curConvo";
	public static final String[] DEFAULT_SOUNDS = {};
	
	
	public static boolean getON_OFF(SharedPreferences prefs){
		return prefs.getBoolean(PreferenceStorage.ON_OFF,false);
	}
	
	public static void setON_OFF(SharedPreferences prefs, boolean b){
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putBoolean(PreferenceStorage.ON_OFF, b);
	    editor.commit();
	}
	
	public static float getAvgConvo(SharedPreferences prefs, String S){
		return prefs.getFloat(PreferenceStorage.MAX_CONVOLUTION+S, 0);
	}
	
	public static void setAverageConvo(SharedPreferences prefs, String S, float f){
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putFloat(PreferenceStorage.MAX_CONVOLUTION+S, f);
	    editor.commit();
	}
	
	public static float getCurConvo(SharedPreferences prefs, String S){
		return prefs.getFloat(PreferenceStorage.CUR_CONVOLUTION+S, 0);
	}
	
	public static void setCurConvo(SharedPreferences prefs, String S, float f){
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putFloat(PreferenceStorage.CUR_CONVOLUTION+S, f);
	    editor.commit();
	}
	
	public static boolean getDetected(SharedPreferences prefs){
		return prefs.getBoolean(PreferenceStorage.DETECTED,false);
	}
	
	public static void setDetected(SharedPreferences prefs, boolean b){
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putBoolean(PreferenceStorage.DETECTED, b);
	    editor.commit();
	}
	
	public static Set<String> getAllSounds(SharedPreferences prefs) {
		Set<String> defaultSet = new HashSet<String>(Arrays.asList(DEFAULT_SOUNDS));
		return prefs.getStringSet(PreferenceStorage.ALL_SOUNDS, defaultSet);
	}
	
	public static Set<String> getAllCheckedSounds(SharedPreferences prefs) {
		HashSet<String> result = new HashSet<String>();
		Iterator<String> it = getAllSounds(prefs).iterator();
		while(it.hasNext()) {
			String soundName = it.next();
			if (isSoundOn(prefs, soundName)) {
				result.add(soundName);
			}
		}
		return result;
	}
	
	public static void addSound(SharedPreferences prefs, String soundName) {
		Set<String> allSounds = getAllSounds(prefs);
		allSounds.add(soundName);
		setAllSounds(prefs, allSounds);
	}
	
	public static void delSound(SharedPreferences prefs, String soundName) {
		Set<String> allSounds = getAllSounds(prefs);
		allSounds.remove(soundName);
		setAllSounds(prefs, allSounds);
	}
	
	public static void setAllSounds(SharedPreferences prefs, Set<String> allSounds) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putStringSet(PreferenceStorage.ALL_SOUNDS, allSounds);
		editor.commit();
	}
	
	public static boolean isSoundOn(SharedPreferences prefs, String soundName) {
		return prefs.getBoolean(soundName+PreferenceStorage.ON, true);
	}
	
	public static boolean isPushNotifOn(SharedPreferences prefs, String soundName) {
		return prefs.getBoolean(soundName+PreferenceStorage.PUSH_NOTIF, true);
	}
	
	public static boolean isAlertNotifOn(SharedPreferences prefs, String soundName) {
		return prefs.getBoolean(soundName+PreferenceStorage.ALERT_NOTIF, true);
	}
	
	public static boolean isVibrateNotifOn(SharedPreferences prefs, String soundName) {
		return prefs.getBoolean(soundName+PreferenceStorage.VIBRATE_NOTIF, true);
	}
	
	public static int getAlertColor(SharedPreferences prefs, String soundName, Resources res) {
		return (int) prefs.getLong(soundName+PreferenceStorage.ALERT_COLOR, res.getColor(R.color.Red));
	}
	
	public static void setSound(SharedPreferences prefs, String soundName, boolean on) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(soundName+PreferenceStorage.ON, on);
    	editor.commit();
	}
	
	public static void setPushNotif(SharedPreferences prefs, String soundName, boolean on) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(soundName+PreferenceStorage.PUSH_NOTIF, on);
    	editor.commit();
	}
	
	public static void setAlertNotif(SharedPreferences prefs, String soundName, boolean on) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(soundName+PreferenceStorage.ALERT_NOTIF, on);
    	editor.commit();
	}
	
	public static void setVibrate(SharedPreferences prefs, String soundName, boolean on) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(soundName+PreferenceStorage.VIBRATE_NOTIF, on);
    	editor.commit();
	}
	
	public static void setColor(SharedPreferences prefs, String soundName, int color) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(soundName+PreferenceStorage.ALERT_COLOR, color);
    	editor.commit();
	}
	

}
