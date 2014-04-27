package com.teck4.so04emod;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class SemcPhonePatcher implements IXposedHookZygoteInit, IXposedHookInitPackageResources {

	private static XSharedPreferences mPreferences;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SemcPhonePatcher.class.getPackage().getName(), Common.PREF_NAME);		
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (resparam.packageName.equals("com.android.phone") == false)
			return;

		if (mPreferences.getBoolean("disable_mobile_date_switch_messages", false)) {
			resparam.res.setReplacement("com.android.phone", "bool", "disable_data_off_popup", true);
			resparam.res.setReplacement("com.android.phone", "bool", "disable_charge_popups", true);
			resparam.res.setReplacement("com.android.phone", "bool", "data_connection_except_mms_show_icon_when_disabled", false);
		}
		
		if (mPreferences.getBoolean("enable_network_mode_lte:wcdma_only", false))
			resparam.res.setReplacement("com.android.phone", "string", "preferred_network_mode_marshal", "12,11,2,1,9");

		if (mPreferences.getBoolean("enable_call_recording", false))
			resparam.res.setReplacement("com.android.phone", "bool", "enable_call_recording", true);
		
	}

}
