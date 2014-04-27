package com.teck4.so04emod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.Menu;
import android.view.MenuItem;

public class Settings extends Activity {
	
	private static SharedPreferences mPreferences;
	private int mSelectedTheme;
	private int mLastTheme;
	private boolean mRestartFlag;

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mPreferences = getSharedPreferences(Common.PREF_NAME, MODE_WORLD_READABLE);
		
		if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, 0) == Common.APP_THEME_DARK) {
			setTheme(R.style.ThemeDark);
		}
		
		getFragmentManager().beginTransaction().replace(android.R.id.content, new Preferences()).commit();
	}

	public static class Preferences extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onResume() {
			super.onResume();

			getPreferenceManager().setSharedPreferencesName(Common.PREF_NAME);
			getPreferenceManager().setSharedPreferencesMode(MODE_WORLD_READABLE);

			if (getPreferenceScreen() != null)
				getPreferenceScreen().removeAll();

			if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, 0) == Common.APP_THEME_DARK) {
				
				addPreferencesFromResource(R.xml.settings_header_dark);
				addPreferencesFromResource(R.xml.settings_header_about_dark);
				
			} else {
				
				addPreferencesFromResource(R.xml.settings_header);
				addPreferencesFromResource(R.xml.settings_header_about);
				
			}
			
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
			case R.id.actions_theme_setting:
				showThemeSelectDialog();
				break;
		}
		
		return true;
	}
	
	private void showThemeSelectDialog() {
		Resources res = getResources();
		mSelectedTheme = mLastTheme = mPreferences.getInt(Common.PREFS_APP_THEME_KEY, Common.APP_THEME_LIGHT);
		final String themes[] = new String[] {
			res.getString(R.string.theme_light),
			res.getString(R.string.theme_dark)
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);

		builder.setTitle(R.string.theme_select_dialog_title);		
		builder.setSingleChoiceItems(themes, mSelectedTheme, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSelectedTheme = which;
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				if (mSelectedTheme != mLastTheme) {
					mPreferences.edit().putInt(Common.PREFS_APP_THEME_KEY, mSelectedTheme)
					.commit();
					mRestartFlag = true;
					finish();
				}
			}
		});
		builder.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mRestartFlag) {
			mRestartFlag = false;
			startActivity(new Intent(this, Settings.class));
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
}

