package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import android.content.Context;
import android.content.res.Resources;
import android.view.Window;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcAlbumPatcher implements IXposedHookLoadPackage, IXposedHookZygoteInit {

	private static XSharedPreferences mPreferences;

	private static boolean mIs40A014 = false;
	private static boolean mIs52A120 = false;
	private static boolean mIs54A020 = false;
	private static boolean mIsNewer = false;
	
	private static Class<?> mClazz;

	private static class FieldName {
		public static String hasSoftNavigationBar = "";
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcAlbumPatcher.class.getPackage().getName(), Common.PREF_NAME);	
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("com.sonyericsson.album") == false)
			return;

		detectAppVersion(lpparam);
		
		if (mIs40A014) {
			return;
		}
		
		if (mIs52A120 || mIs54A020 || mIsNewer) {
			if (mPreferences.getBoolean("disable_unnecessary_menu_key", false) &&
					mPreferences.getString("navigation_bar_style", "").isEmpty() == false) {
				
				if (mIs52A120) {
					mClazz = findClass("com.sonyericsson.album.util.NavigationBarUtils", lpparam.classLoader);
					FieldName.hasSoftNavigationBar = "sHasSemcSoftNavigationBar";
				}else if (mIs54A020 || mIsNewer) {
					mClazz = findClass("com.sonyericsson.album.util.BarUtils", lpparam.classLoader);
					FieldName.hasSoftNavigationBar = "sHasSoftNavigationBar";
				}

				findAndHookMethod(mClazz, "init", Context.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						super.afterHookedMethod(param);
						
						setStaticObjectField(mClazz, FieldName.hasSoftNavigationBar, true);
						Resources res = Resources.getSystem();
						int idNavBarHeightPortrait = res.getIdentifier("navigation_bar_height", "dimen", "android");
						int idNavBarHeightLandscape = res.getIdentifier("navigation_bar_width", "dimen", "android");
						int idStatusBarHeight = res.getIdentifier("status_bar_height", "dimen", "android");
						setStaticObjectField(mClazz, "sNavbarHeightPortrait", res.getDimension(idNavBarHeightPortrait));
						setStaticObjectField(mClazz, "sNavbarHeightLandscape", res.getDimension(idNavBarHeightLandscape));
						setStaticObjectField(mClazz, "sStatusBarHeight", res.getDimensionPixelSize(idStatusBarHeight));
						setStaticObjectField(mClazz, "sIsStatusBarAvailable", true);

					}
				});

			}
			
			if (mPreferences.getBoolean("transparent_ui", false)) {
				if (mIsNewer) {
					setStaticObjectField(mClazz, "sSupportsTranslucentStatusBar", true);
				} else {
					setStaticObjectField(mClazz, "sIsNavigationBarTransparent", true);
				}
			}
			
			return;
		}
		
	}

	private void detectAppVersion(LoadPackageParam lpp) {
		try {
			
			Class<?> clazz = findClass("com.sonyericsson.album.util.NavigationBarUtils", lpp.classLoader);
			try {
				findMethodExact(clazz, "disableStatusBar", Window.class);
				mIs52A120 = true;
				return;
			} catch (NoSuchMethodError e) {
				mIs40A014 = true;
				return;
			}
			
		} catch (ClassNotFoundError e) {
			
			try {
				Class<?> clazz = findClass("com.sonyericsson.album.util.BarUtils", lpp.classLoader);
				try {
					getStaticObjectField(clazz, "sIsNavigationBarTransparent");
					mIs54A020 = true;					
					return;
				} catch (Error e2) {
				} catch (Exception e2) {
				}
				mIsNewer = true;
				return;
			} catch (ClassNotFoundError e2){
			}
			
		}
		
	}

}
