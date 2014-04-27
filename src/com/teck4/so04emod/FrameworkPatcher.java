package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.content.res.XResources;
import android.os.BatteryManager;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;

public class FrameworkPatcher implements IXposedHookZygoteInit {

	private static XSharedPreferences mPreferences;

	private static int mBatteryLevel;
	private static int mBatteryStatus;
	private static int mLowBatteryWarningLevel;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(FrameworkPatcher.class.getPackage().getName(), Common.PREF_NAME);
		
		
		
		if (mPreferences.getBoolean("disable_unnecessary_menu_key", false) &&
				mPreferences.getString("navigation_bar_style", "").isEmpty() == false) {
			findAndHookMethod("android.view.ViewConfiguration", FrameworkPatcher.class.getClassLoader(), "hasPermanentMenuKey", XC_MethodReplacement.returnConstant(true));
		}

		
		
		if (mPreferences.getString("battery_maximum_led_color", "").isEmpty() == false ||
				mPreferences.getString("battery_warning_led_color", "").isEmpty() == false ){

			findAndHookMethod("com.android.server.BatteryService", FrameworkPatcher.class.getClassLoader(), "processValuesLocked", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					super.beforeHookedMethod(param);
					mBatteryLevel = (Integer)getObjectField(param.thisObject, "mBatteryLevel");
					mBatteryStatus = (Integer)getObjectField(param.thisObject, "mBatteryStatus");
					mLowBatteryWarningLevel = (Integer)getObjectField(param.thisObject, "mLowBatteryWarningLevel");
				}

			});

			findAndHookMethod("com.android.server.BatteryService.Led", FrameworkPatcher.class.getClassLoader(), "updateLightsLocked", new XC_MethodReplacement() {

				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

					final int level = mBatteryLevel;
					final int status = mBatteryStatus;
					final int LIGHT_FLASH_TIMED = 1;
					final Object mBatteryLight = getObjectField(param.thisObject, "mBatteryLight");

					int mBatteryFullARGB = (Integer)getObjectField(param.thisObject, "mBatteryFullARGB");
					int mBatteryMediumARGB = (Integer)getObjectField(param.thisObject, "mBatteryMediumARGB");
					int mBatteryLowARGB = (Integer)getObjectField(param.thisObject, "mBatteryLowARGB");
					int mBatteryLedOn = (Integer)getObjectField(param.thisObject, "mBatteryLedOn");
					int mBatteryLedOff = (Integer)getObjectField(param.thisObject, "mBatteryLedOff");

					int mBatteryMaximumARGB = (Integer)getObjectField(param.thisObject, "mBatteryFullARGB");
					int mBatteryWarningARGB = (Integer)getObjectField(param.thisObject, "mBatteryLowARGB");
					
					if (mPreferences.getString("battery_maximum_led_color", "").isEmpty() == false)
						mBatteryMaximumARGB = ((Long)Long.parseLong(mPreferences.getString("battery_maximum_led_color", ""), 16)).intValue();
					if (mPreferences.getString("battery_warning_led_color", "").isEmpty() == false)
						mBatteryWarningARGB = ((Long)Long.parseLong(mPreferences.getString("battery_warning_led_color", ""), 16)).intValue();

					if (level < mLowBatteryWarningLevel) {
						if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
							callMethod(mBatteryLight, "setColor", mBatteryLowARGB);
						} else {
							callMethod(mBatteryLight, "setFlashing", mBatteryWarningARGB, LIGHT_FLASH_TIMED, mBatteryLedOn, mBatteryLedOff);
						}
					} else if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL) {
						if (status == BatteryManager.BATTERY_STATUS_FULL) {
							callMethod(mBatteryLight, "setColor", mBatteryMaximumARGB);
						} else if (level >= 90) {
							callMethod(mBatteryLight, "setColor", mBatteryFullARGB);
						} else {
							callMethod(mBatteryLight, "setColor", mBatteryMediumARGB);
						}
					} else {
						callMethod(mBatteryLight, "turnOff");
					}

					return null;
				}
			});

		}

		
		
		// Battery LED
		if (mPreferences.getString("battery_high_led_color", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_notificationsBatteryFullARGB", ((Long)Long.parseLong(mPreferences.getString("battery_high_led_color", ""), 16)).intValue());
		if (mPreferences.getString("battery_medium_led_color", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_notificationsBatteryMediumARGB", ((Long)Long.parseLong(mPreferences.getString("battery_medium_led_color", ""), 16)).intValue());
		if (mPreferences.getString("battery_low_led_color", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_notificationsBatteryLowARGB", ((Long)Long.parseLong(mPreferences.getString("battery_low_led_color", ""), 16)).intValue());
		if (mPreferences.getString("battery_led_flash_on", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_notificationsBatteryLedOn", Integer.parseInt(mPreferences.getString("battery_led_flash_on", "")));
		if (mPreferences.getString("battery_led_flash_off", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_notificationsBatteryLedOff", Integer.parseInt(mPreferences.getString("battery_led_flash_off", "")));
		
		// Notification LED
		if (mPreferences.getString("notification_led_color", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "color", "config_defaultNotificationColor", ((Long)Long.parseLong(mPreferences.getString("notification_led_color", ""), 16)).intValue());
		if (mPreferences.getString("notification_led_flash_on", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_defaultNotificationLedOn", Integer.parseInt(mPreferences.getString("notification_led_flash_on", "")));
		if (mPreferences.getString("notification_led_flash_off", "").isEmpty() == false)
			XResources.setSystemWideReplacement("android", "integer", "config_defaultNotificationLedOff", Integer.parseInt(mPreferences.getString("notification_led_flash_off", "")));

		if (mPreferences.getBoolean("enable_all_screen_rotations", false))
			XResources.setSystemWideReplacement("android", "bool", "config_allowAllRotations", true);
		
		if (mPreferences.getBoolean("enable_lock_screen_rotation", false)) {
			XResources.setSystemWideReplacement("android", "bool", "config_enableLockScreenRotation", true);
			XResources.setSystemWideReplacement("android", "bool", "lockscreen_isPortrait", false);
			XResources.setSystemWideReplacement("android", "bool", "config_enableLockscreenExpandedWidgetFrame", false);
		}

		if (mPreferences.getBoolean("disable_volume_key_sound", false))
			XResources.setSystemWideReplacement("android", "bool", "config_useVolumeKeySounds", false);
		
		if (mPreferences.getBoolean("disable_safe_media_volume", false))
			XResources.setSystemWideReplacement("android", "bool", "config_safe_media_volume_enabled", false);
	}

}
