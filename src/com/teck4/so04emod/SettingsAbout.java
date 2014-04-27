package com.teck4.so04emod;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsAbout extends Activity {
	
	private static SharedPreferences mPreferences;
	private Resources mResources;
	private AlertDialog.Builder mBuilder;

	private boolean mSuperUser = false;
	private Process mShell = null;
	private DataInputStream mInputStream = null;
	private DataOutputStream mOutputStream = null;
	
	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPreferences = getSharedPreferences(Common.PREF_NAME, MODE_WORLD_READABLE);
		mResources = getResources();
		
		try {
			mShell = Runtime.getRuntime().exec("sh");
			mInputStream = new DataInputStream(mShell.getInputStream());
			mOutputStream = new DataOutputStream(mShell.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, Common.APP_THEME_LIGHT) == Common.APP_THEME_DARK) {
			setTheme(R.style.ThemeDark);
		}
		
		setContentView(R.layout.settings_about);

		mBuilder = new AlertDialog.Builder(this);
		
		if (mPreferences.getInt(Common.PREFS_APP_THEME_KEY, Common.APP_THEME_LIGHT) == Common.APP_THEME_DARK) {
			((TextView)findViewById(R.id.title_developer)).setTextColor(mResources.getColor(android.R.color.holo_blue_dark));
			((TextView)findViewById(R.id.title_version)).setTextColor(mResources.getColor(android.R.color.holo_blue_dark));
		}
		((TextView)findViewById(R.id.developer_name)).getPaint().setUnderlineText(true);
		((TextView)findViewById(R.id.version_name)).getPaint().setUnderlineText(true);

		findViewById(R.id.reboot_button).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				mBuilder.setTitle(R.string.settings_about_reboot_button_text);
				mBuilder.setMessage(R.string.settings_about_reboot_confirm);
				mBuilder.setPositiveButton(android.R.string.ok, dialogPositiveButtonClickListener);
				mBuilder.setNegativeButton(android.R.string.cancel, null);
				
				AlertDialog dialog = mBuilder.create();
				dialog.show();

				LinearLayout contentPanel = (LinearLayout)dialog.getWindow().getDecorView().findViewById(mResources.getIdentifier("contentPanel", "id", "android"));
				if (contentPanel != null) contentPanel.setGravity(Gravity.CENTER_VERTICAL);
			}
		});

	}

	private DialogInterface.OnClickListener dialogPositiveButtonClickListener = new DialogInterface.OnClickListener(){
		@Override
		public void onClick(DialogInterface dialog, int which) {
			setSuperUser(true);
			if (mSuperUser) writeStream("reboot\n");
		}
	};

	private String readStream() {
		String res = "";

		try{
			if (mInputStream != null) {
				byte[] buffer = new byte[64];
				int size = mInputStream.read(buffer);
				if (0 < size)
					res = new String(buffer, 0, size - 1);
			}
		}catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	private void writeStream(String command) {
		try {
			if (mOutputStream == null)
				return;
			
			mOutputStream.writeBytes(command);
			mOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean setSuperUser(boolean on) {
		if (mSuperUser == on)
			return true;
		
		if (on) {
			writeStream("su\n");
			writeStream("whoami\n");
			String res = readStream();
			mSuperUser = res.equals("root");
		} else {
			writeStream("exit\n");
			mSuperUser = false;
		}
		
		return mSuperUser;
	}

	public void onClickDeveloper(View v) {
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + mResources.getString(R.string.twitter_screen_name))));
	}

}
