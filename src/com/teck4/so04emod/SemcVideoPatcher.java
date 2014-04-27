package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcVideoPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage {
	
	private static XSharedPreferences mPreferences;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcVideoPatcher.class.getPackage().getName(), Common.PREF_NAME);	
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("com.sonyericsson.video") == false)
			return;

		if (mPreferences.getBoolean("disable_unnecessary_menu_key", false) &&
				mPreferences.getString("navigation_bar_style", "").isEmpty() == false) {
			findAndHookMethod("com.sonyericsson.video.common.DeviceProperty", lpparam.classLoader, "hasHWMenuKey", Context.class, XC_MethodReplacement.returnConstant(false));
		}
		
	}

}
