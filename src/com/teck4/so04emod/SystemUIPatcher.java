package com.teck4.so04emod;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import java.util.Calendar;
import java.util.TimeZone;
import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.graphics.Typeface;
import android.media.MediaActionSound;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class SystemUIPatcher implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources{

	final int mResIDSearch	= 0x7e47439f;
	final int mResIDPower	= 0x7e92902e;

	private static String MODULE_PATH = null;
	private static XSharedPreferences mPreferences = null;

	private static TextView mStatusBarClockView = null;
	
	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		MODULE_PATH = startupParam.modulePath;
		mPreferences = new XSharedPreferences(SystemUIPatcher.class.getPackage().getName(), Common.PREF_NAME);
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
		if (resparam.packageName.equals("com.android.systemui") == false)
			return;

		XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);

		resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
			@Override
			public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
				mStatusBarClockView = (TextView) liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", "com.android.systemui"));
				
				if (mPreferences.getString("clock_font_size", "").equals(Common.CLOCK_FONT_SIZE_LARGE)) {
					mStatusBarClockView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19.0f);
				} else if (mPreferences.getString("clock_font_size", "").equals(Common.CLOCK_FONT_SIZE_SMALL)) {
					mStatusBarClockView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.0f);
				} else if (mPreferences.getString("clock_font_size", "").equals(Common.CLOCK_FONT_SIZE_EXTRA_SMALL)) {
					mStatusBarClockView.setLineSpacing(mStatusBarClockView.getLineSpacingExtra(), 0.8f);
					mStatusBarClockView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.0f);
				}

				if (mPreferences.getString("clock_format", "").indexOf("\n") != -1) {
					mStatusBarClockView.setSingleLine(false);
					mStatusBarClockView.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
				}
				
				if (mPreferences.getBoolean("clock_bold", false)) {
					mStatusBarClockView.setTypeface(mStatusBarClockView.getTypeface(), Typeface.BOLD);
				}
			}
		});

		if (mPreferences.getBoolean("transparent_ui", false)) {
			resparam.res.setReplacement("com.android.systemui", "color", "system_ui_opaque_background", 0x99000000);
			resparam.res.setReplacement("com.android.systemui", "color", "system_ui_transparent_background", 0x80000000);
		}
		
		if (mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_3BUTTON)) {
			resparam.res.setReplacement("com.android.systemui", "layout", "navigation_bar", modRes.fwd(R.layout.navigation_bar_mod_3bt));
		} else if (mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_5BUTTON_SEARCH)) {
			resparam.res.setReplacement("com.android.systemui", "layout", "navigation_bar", modRes.fwd(R.layout.navigation_bar_mod_5bt_search));
		} else if (mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_5BUTTON_POWER)) {
			resparam.res.setReplacement("com.android.systemui", "layout", "navigation_bar", modRes.fwd(R.layout.navigation_bar_mod_5bt_power));
		}
		
		if (mPreferences.getBoolean("no_battery_text", false)) {
			resparam.res.hookLayout("com.android.systemui", "layout", "super_status_bar", new XC_LayoutInflated() {
				@Override
				public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
					liparam.view.findViewById(liparam.res.getIdentifier("battery_text", "id", "com.android.systemui")).setVisibility(View.GONE);
				}
			});
		}

		if (mPreferences.getBoolean("center_clock", false)) {
			resparam.res.setReplacement("com.android.systemui", "layout", "status_bar", modRes.fwd(R.layout.status_bar_mod_clock_center));
		}

	}
	
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (lpparam.packageName.equals("com.android.systemui") == false)
			return;

		if (mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_3BUTTON) ||
				mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_5BUTTON_SEARCH) ||
				mPreferences.getString("navigation_bar_style", "").equals(Common.NAV_BAR_5BUTTON_POWER) ) {
			
			findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "setMenuVisibility", boolean.class, boolean.class, XC_MethodReplacement.DO_NOTHING);
			
			findAndHookMethod("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader, "setDisabledFlags", int.class, boolean.class, new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
					int disabledFlags = (Integer)param.args[0];
					boolean force = (Boolean)param.args[1];

					final int STATUS_BAR_DISABLE_HOME		= 0x00200000;
					final int STATUS_BAR_DISABLE_RECENT	= 0x01000000;
					final int STATUS_BAR_DISABLE_BACK		= 0x00400000;
					final int STATUS_BAR_DISABLE_SEARCH	= 0x02000000;
					final int NAVIGATION_HINT_BACK_ALT		= 1 << 3;

					int mDisabledFlags = (Integer)getObjectField(param.thisObject, "mDisabledFlags");
					int mNavigationIconHints = (Integer)getObjectField(param.thisObject, "mNavigationIconHints");
					boolean mScreenOn = (Boolean)getObjectField(param.thisObject, "mScreenOn");
					View mCurrentView = (View)getObjectField(param.thisObject, "mCurrentView");					

					Context mContext = (Context)getObjectField(param.thisObject, "mContext");
					Resources res = mContext.getResources();

					if (!force && mDisabledFlags == disabledFlags)
						return null;

					setObjectField(param.thisObject, "mDisabledFlags", disabledFlags);

					final boolean disableHome = ((disabledFlags & STATUS_BAR_DISABLE_HOME) != 0);
					final boolean disableRecent = ((disabledFlags & STATUS_BAR_DISABLE_RECENT) != 0);					
					final boolean disableBack = ((disabledFlags & STATUS_BAR_DISABLE_BACK) != 0)
							&& ((mNavigationIconHints & NAVIGATION_HINT_BACK_ALT) == 0);
					final boolean disableSearch = ((disabledFlags & STATUS_BAR_DISABLE_SEARCH) != 0);

					callMethod(param.thisObject, "setSlippery", disableHome && disableRecent && disableBack && disableSearch);

					if (!mScreenOn && mCurrentView != null) {
						ViewGroup navButtons = (ViewGroup) mCurrentView.findViewById(res.getIdentifier("nav_buttons", "id", "com.android.systemui"));
						LayoutTransition lt = navButtons == null ? null : navButtons.getLayoutTransition();
						if (lt != null) {
							lt.disableTransitionType(
									LayoutTransition.CHANGE_APPEARING | LayoutTransition.CHANGE_DISAPPEARING |
									LayoutTransition.APPEARING | LayoutTransition.DISAPPEARING);
						}
					}

					View back = mCurrentView.findViewById(res.getIdentifier("back", "id", "com.android.systemui"));
					View home = mCurrentView.findViewById(res.getIdentifier("home", "id", "com.android.systemui"));
					View recent = mCurrentView.findViewById(res.getIdentifier("recent_apps", "id", "com.android.systemui"));
					View searchLight = mCurrentView.findViewById(res.getIdentifier("search_light", "id", "com.android.systemui"));

					if (back != null)				back.setVisibility(disableBack ? View.INVISIBLE : View.VISIBLE);
					if (home != null)				home.setVisibility(disableHome ? View.INVISIBLE : View.VISIBLE);
					if (recent != null)			recent.setVisibility(disableRecent ? View.INVISIBLE : View.VISIBLE);
					if (searchLight != null)	searchLight.setVisibility((disableHome && !disableSearch) ? View.VISIBLE : View.GONE);

					final boolean disableMenu = disableRecent;
					final boolean disableSearchButton = disableRecent;
					final boolean disablePower = disableRecent;
					View menu = mCurrentView.findViewById(res.getIdentifier("menu", "id", "com.android.systemui"));
					View search = mCurrentView.findViewById(mResIDSearch);
					View power = mCurrentView.findViewById(mResIDPower);
					if (menu != null)		menu.setVisibility(disableMenu ? View.INVISIBLE : View.VISIBLE);
					if (search != null)	search.setVisibility(disableSearchButton ? View.INVISIBLE : View.VISIBLE);
					if (power != null)	power.setVisibility(disablePower ? View.INVISIBLE : View.VISIBLE);

					return null;
				}
			});

		}
		
		if (mPreferences.getBoolean("disable_search_panel", false)) {
			findAndHookMethod("com.android.systemui.statusbar.phone.PhoneStatusBar", lpparam.classLoader, "shouldDisableNavbarGestures", XC_MethodReplacement.returnConstant(true));
		}
		
		if (mPreferences.getBoolean("disable_screenshot_sound", false)) {
			findAndHookMethod("com.android.systemui.screenshot.GlobalScreenshot", lpparam.classLoader, "startAnimation", Runnable.class, int.class, int.class, boolean.class, boolean.class, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					super.beforeHookedMethod(param);
					setObjectField(param.thisObject, "mCameraSound", new DummyMediaActionSound());		
				}

			});
		}
		
		
		if (mPreferences.getString("clock_format", "").isEmpty() == false) {
			findAndHookMethod("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader, "updateClock", new XC_MethodHook(){
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);

					Calendar mCalendar = Calendar.getInstance(TimeZone.getDefault());
					CharSequence result = DateFormat.format(mPreferences.getString("clock_format", ""), mCalendar);
					mStatusBarClockView.setText(result);

				}
			});
		}
		
		if (mPreferences.getBoolean("disable_battery_warning_dialog", false)) {
			findAndHookMethod("com.android.systemui.power.PowerUI", lpparam.classLoader, "findBatteryLevelBucket", int.class, XC_MethodReplacement.returnConstant(1));
		}
	}
	
	class DummyMediaActionSound extends MediaActionSound {
		@Override
		public synchronized void play(int soundName) {
			return;
		}
	}
	
}
