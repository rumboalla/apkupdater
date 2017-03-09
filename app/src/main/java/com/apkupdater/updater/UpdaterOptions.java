package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.apkupdater.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterOptions
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterOptions(
		Context context
	) {
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean skipExperimental(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_skip_experimental_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useAPKMirror(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_apkmirror_key), true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useUptodown(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_uptodown_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useAPKPure(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_apkpure_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<String> getIgnoreList(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		String s = sharedPref.getString(mContext.getString(R.string.preferences_general_ignorelist_key), mContext.getString(R.string.preferences_general_ignorelist_value));

		// Fill the list if it's not empty
		List<String> list = new ArrayList<>();
		//noinspection ConstantConditions
		if (s != null && !s.isEmpty()) { //s actually can be null, contrary to what AS says
			String [] strings = s.split(",");
			Collections.addAll(list, strings);
		}

		return list;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setIgnoreList(
		List<String> l
	) {
		// Make a comma separated string from the list
		String s = "";
		for(String i : l) {
			s += i + ",";
		}
		s = s.substring(0, s.length() - 1);

		// Add it to shared prefs
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		sharedPref.edit().putString(
			mContext.getString(R.string.preferences_general_ignorelist_key),
			s
		).apply();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getNotificationOption(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_notification_key), mContext.getString(R.string.notification_always));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getAlarmOption(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_alarm_key), mContext.getString(R.string.alarm_daily));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getNumThreads(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		String v = sharedPref.getString(mContext.getString(R.string.preferences_general_num_threads_key), mContext.getString(R.string.num_threads_five));
		return Integer.valueOf(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getTheme(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_theme_key), mContext.getString(R.string.theme_blue));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean getExcludeSystemApps(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_exclude_system_apps_key), true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean getExcludeDisabledApps(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_exclude_disabled_apps_key), true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
