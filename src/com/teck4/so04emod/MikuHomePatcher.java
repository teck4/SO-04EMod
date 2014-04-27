package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MikuHomePatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	private static XSharedPreferences mPreferences;
	private static boolean mFirst = true;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(MikuHomePatcher.class.getPackage().getName(), Common.PREF_NAME);
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		
		if (lpparam.packageName.equals("com.mikuxperia.mikuhomeapp") == false)
			return;
		
		if (mPreferences.getBoolean("transparent_ui", false)) {
			
			findAndHookMethod("com.mikuxperia.mikuhomeapp.dM", lpparam.classLoader, "run", XC_MethodReplacement.DO_NOTHING);
			
			findAndHookMethod("com.mikuxperia.mikuhomeapp.MikuLauncher", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					super.beforeHookedMethod(param);
					if (mFirst)
						setObjectField(param.thisObject, "aq", 1);
				}
				
			});
			
			findAndHookMethod("com.mikuxperia.mikuhomeapp.MikuLauncher", lpparam.classLoader, "onResume", new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					if (mFirst) {
						setObjectField(param.thisObject, "aq", 0);
						mFirst = false;
					} else {
						int validate = (Integer)getObjectField(param.thisObject, "aq");
						if (validate == 0) {
							callMethod(param.thisObject, "finish");
						}
					}
				}
				
			});

		}

	}

}
