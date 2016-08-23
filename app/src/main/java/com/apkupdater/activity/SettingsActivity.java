package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;

import com.apkupdater.R;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.AlarmUtil;

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

	@MainThread
	private void changeTheme(
	) {
		SettingsActivity_.intent(this).flags(FLAG_ACTIVITY_CLEAR_TOP).start();

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setThemeFromOptions(
	) {
		UpdaterOptions options = new UpdaterOptions(mContext);
		if(options.getTheme().equals(getString(R.string.theme_blue))) {
			setTheme(R.style.PreferenceScreenThemeBlue);
		} else if (options.getTheme().equals(getString(R.string.theme_dark))) {
			setTheme(R.style.AppThemeDark);
		} else if (options.getTheme().equals(getString(R.string.theme_light))) {
			setTheme(R.style.AppThemeLight);
		}else if (options.getTheme().equals(getString(R.string.theme_orange))) {
			setTheme(R.style.PreferenceScreenThemeOrange);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(
		Bundle savedInstanceState
	) {
		mContext = getBaseContext();
		setThemeFromOptions();

		super.onCreate(savedInstanceState);

		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(mChanges);
		addPreferencesFromResource(R.xml.preferences);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	protected void onResume() {
		super.onResume();
	}
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////