package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcCamera3DPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage {

	private static XSharedPreferences mPreferences;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcCamera3DPatcher.class.getPackage().getName(), Common.PREF_NAME);
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("com.sonyericsson.android.camera3d") == false)
			return;

		if (mPreferences.getBoolean("enable_camera_sound_option", false)) {
			findAndHookMethod("com.sonyericsson.android.camera3d.CameraConfigManager", lpparam.classLoader, "setForceSound", String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					setBooleanField(param.thisObject, "mIsForceSound", false);
				}
			});
		}

	}

}
