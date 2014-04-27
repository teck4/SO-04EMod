package com.teck4.so04emod;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;

public class SettingsPatcher implements IXposedHookZygoteInit, IXposedHookInitPackageResources {

	private static XSharedPreferences mPreferences;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(SettingsPatcher.class.getPackage().getName(), Common.PREF_NAME);		
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		
		if (resparam.packageName.equals("com.android.settings") == false)
			return;
		
		if (mPreferences.getBoolean("disable_mobile_date_switch_dialog", false))
			resparam.res.setReplacement("com.android.settings", "bool", "config_showMobileDataCostWarning", false);
		
		if (mPreferences.getBoolean("enable_hide_tethering_dialog", false))
			resparam.res.setReplacement("com.android.settings", "bool", "config_showTetheringDialogCheckbox", true);		
	}

}
