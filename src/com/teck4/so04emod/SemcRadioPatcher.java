package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcRadioPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	private static XSharedPreferences mPreferences;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcRadioPatcher.class.getPackage().getName(), Common.PREF_NAME);		
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		if (lpparam.packageName.equals("com.sonyericsson.fmradio") == false)
			return;

		if (mPreferences.getBoolean("fmradio_without_headset", false))
			findAndHookMethod("com.sonyericsson.fmradio.service.PhfHandler", lpparam.classLoader, "isPhfConnected", XC_MethodReplacement.returnConstant(true));

	}

}
