package com.teck4.so04emod;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsNetwork extends Activity {

	private static SharedPreferences mPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(Common.PREF_NAME, MODE_PRIVATE);
		
		if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, Common.APP_THEME_LIGHT) == Common.APP_THEME_DARK) {
			setTheme(R.style.ThemeDark);
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
			addPreferencesFromResource(R.xml.settings_network);
		}

	}

}
