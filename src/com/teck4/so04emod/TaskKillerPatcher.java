package com.teck4.so04emod;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.RelativeLayout;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;


public class TaskKillerPatcher implements IXposedHookZygoteInit, IXposedHookInitPackageResources {

	private static XSharedPreferences mPreferences;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(TaskKillerPatcher.class.getPackage().getName(), Common.PREF_NAME);
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {

		if (resparam.packageName.equals("com.sonymobile.taskkiller") == false)
			return;

		if (mPreferences.getBoolean("change_position_kill_all_button", false)) {

			resparam.res.hookLayout("com.sonymobile.taskkiller", "layout", "task_killer_button_panel", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					Resources res = liparam.view.getResources();
					Button taskKillerButton = (Button)liparam.view.findViewById(liparam.res.getIdentifier("task_killer_button", "id", "com.sonymobile.taskkiller"));
					RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)taskKillerButton.getLayoutParams();
					
					switch (res.getConfiguration().orientation) {
						case Configuration.ORIENTATION_LANDSCAPE:
							params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
							params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
							params.setMargins(
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, res.getDisplayMetrics())
							);
							break;
						case Configuration.ORIENTATION_PORTRAIT:
						default:
							params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
							params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
							params.setMargins(
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -1, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, res.getDisplayMetrics()),
									(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, res.getDisplayMetrics())
							);
							break;
					}
					
				}
			}); 
			
		}

	}

}
