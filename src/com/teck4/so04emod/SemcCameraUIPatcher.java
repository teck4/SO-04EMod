package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcCameraUIPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	private static XSharedPreferences mPreferences;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcCameraUIPatcher.class.getPackage().getName(), Common.PREF_NAME);
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if(lpparam.packageName.equals("com.sonyericsson.android.camera") == false)
			return;

		if (mPreferences.getBoolean("enable_camera_sound_option", false)) {
			findAndHookMethod("com.sonyericsson.android.camera.configuration.SystemPropertiesReader", lpparam.classLoader, "isForceSound", XC_MethodReplacement.returnConstant(false));
		}
		
	}

}
