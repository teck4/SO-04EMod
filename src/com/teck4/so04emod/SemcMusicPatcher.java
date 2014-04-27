package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findMethodsByExactParameters;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setStaticObjectField;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SemcMusicPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage{

	private static XSharedPreferences mPreferences;
	private boolean initUiUtilsHooked = false;
	private static Class<?> mClazz;

	private static class FieldName {
		public static String navigationBarHeight = "";
		public static String navigationBarWidth = "";
		public static String statusBarHeight = "";
	}
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcMusicPatcher.class.getPackage().getName(), Common.PREF_NAME);		
	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("com.sonyericsson.music") == false)
			return;

		if (mPreferences.getBoolean("disable_unnecessary_menu_key", false) &&
				mPreferences.getString("navigation_bar_style", "").isEmpty() == false) {
			
			XC_MethodHook initUiUtils = new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					
					Resources res = Resources.getSystem();
					int idNavigationBarHeight = res.getIdentifier("navigation_bar_height", "dimen", "android");
					int idNavigationBarWidth = res.getIdentifier("navigation_bar_width", "dimen", "android");
					int idStatusBarHeight = res.getIdentifier("status_bar_height", "dimen", "android");
					setStaticObjectField(mClazz, FieldName.navigationBarHeight, res.getDimensionPixelSize(idNavigationBarHeight));
					setStaticObjectField(mClazz, FieldName.navigationBarWidth, res.getDimensionPixelSize(idNavigationBarWidth));
					setStaticObjectField(mClazz, FieldName.statusBarHeight, res.getDimensionPixelSize(idStatusBarHeight));
					
				}
			};
			
			// 8.3.A.0.2
			if (initUiUtilsHooked == false) {
				try {
					mClazz = findClass("com.sonyericsson.music.common.dj", lpparam.classLoader);
					FieldName.navigationBarHeight = "b";
					FieldName.navigationBarWidth = "c";
					FieldName.statusBarHeight = "a";
					findAndHookMethod(mClazz, "c", Context.class, initUiUtils);
					initUiUtilsHooked = true;
				} catch (Error e) {}
			}
			
			// 8.1.A.0.3
			if (initUiUtilsHooked == false) {
				try {
					mClazz = findClass("com.sonyericsson.music.common.co", lpparam.classLoader);
					FieldName.navigationBarHeight = "b";
					FieldName.navigationBarWidth = "c";
					FieldName.statusBarHeight = "a";
					findAndHookMethod(mClazz, "a", Context.class, initUiUtils);
					initUiUtilsHooked = true;
				} catch (Error e) {}
			}

			// 8.0.A.0.4
			if (initUiUtilsHooked == false) {
				try {
					mClazz = findClass("com.sonyericsson.music.common.ck", lpparam.classLoader);
					FieldName.navigationBarHeight = "b";
					FieldName.navigationBarWidth = "c";
					FieldName.statusBarHeight = "a";
					findAndHookMethod(mClazz, "a", Context.class, initUiUtils);
					initUiUtilsHooked = true;
				} catch (Error e) {}
			}

			// 7.15.A.0.0
			if (initUiUtilsHooked == false) {
				try {
					mClazz = findClass("com.sonyericsson.music.common.cd", lpparam.classLoader);
					FieldName.navigationBarHeight = "b";
					FieldName.navigationBarWidth = "c";
					FieldName.statusBarHeight = "a";
					findAndHookMethod(mClazz, "a", Context.class, initUiUtils);
					initUiUtilsHooked = true;
				} catch (Error e) {}
			}

			// Auto hook.
			if (initUiUtilsHooked == false) {
				FieldName.navigationBarHeight = "b";
				FieldName.navigationBarWidth = "c";
				FieldName.statusBarHeight = "a";
				search_hook:
					for (char i='`'; i<='z'; i++) {
						String className1 = String.valueOf(i);
						if (className1.equals("`")) {
							className1 = "";
						}
						for (char j='a'; j<='z'; j++) {	
							String className2 = String.valueOf(j);

							try {
								mClazz = findClass("com.sonyericsson.music.common." + className1 + className2, lpparam.classLoader);
								try {
									getStaticIntField(mClazz, "a");
									getStaticIntField(mClazz, "b");
									getStaticIntField(mClazz, "c");
									Method[] methods = findMethodsByExactParameters(mClazz, void.class, Context.class);
									XposedBridge.hookMethod(methods[0], initUiUtils);
									XposedBridge.log("[SemcMusicPatcher]Hooked! \"com.sonyericsson.music.common." + className1 + className2 + "->" + methods[0].getName() + "()\"");
									initUiUtilsHooked = true;
									break search_hook;
								} catch (Error e) {
								} catch (Exception e) {
								}
							} catch (ClassNotFoundError e) {
								XposedBridge.log("[SemcMusicPatcher]Hook target not found...");
								break search_hook;
							}
						}
					}
			}
			
		}
	}
}
