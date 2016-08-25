package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;

import com.apkupdater.R;
import com.apkupdater.util.AlarmUtil;
import com.apkupdater.util.ThemeUtil;

import org.androidannotations.annotations.EActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EActivity
public class SettingsActivity
	extends PreferenceActivity
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	SharedPreferences.OnSharedPreferenceChangeListener mChanges = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences,
			String key
		) {
			// Change alarm
			if (key.equals(getString(R.string.preferences_general_alarm_key))) {
				AlarmUtil alarmUtil = new AlarmUtil(mContext);
				alarmUtil.setAlarmFromOptions();
			} else if (key.equals(getString(R.string.preferences_general_theme_key))) {
				changeTheme();
			}
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@MainThread
	private void changeTheme(
	) {
		SettingsActivity_.intent(this).flags(FLAG_ACTIVITY_CLEAR_TOP).start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(
		Bundle savedInstanceState
	) {
		mContext = getBaseContext();
		setTheme(ThemeUtil.getSettingsThemeFromOptions(mContext));
		super.onCreate(savedInstanceState);
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(mChanges);
		addPreferencesFromResource(R.xml.preferences);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////