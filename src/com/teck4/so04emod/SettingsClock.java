package com.teck4.so04emod;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsClock extends Activity {
	
	private static SharedPreferences mPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(Common.PREF_NAME, MODE_PRIVATE);
		
		if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, Common.APP_THEME_LIGHT) == Common.APP_THEME_DARK) {
			setTheme(android.R.style.Theme_Holo_Dialog);
		}
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new Preferences()).commit();
	}

	public static class Preferences extends PreferenceFragment {

		@SuppressWarnings("deprecation")
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(Common.PREF_NAME);
			getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);
			addPreferencesFromResource(R.xml.settings_clock);
			updatePreferenceScreen();
		}

		public void updatePreferenceScreen() {
		}

		OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1){
				updatePreferenceScreen();
			}

		};

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
		}
	}

}
