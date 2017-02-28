package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.apkupdater.R;
import com.apkupdater.activity.MainActivity_;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.util.AlarmUtil;
import com.apkupdater.util.MyBus;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment
public class SettingsFragment
	extends PreferenceFragmentCompat
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean
	MyBus mBus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreatePreferences(
		Bundle savedInstanceState,
		String rootKey
	) {
		PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(mChanges);
		addPreferencesFromResource(R.xml.preferences);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	SharedPreferences.OnSharedPreferenceChangeListener mChanges = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences,
			String key
		) {
			try {
				// Change alarm
				if (key.equals(getString(R.string.preferences_general_alarm_key))) {
					AlarmUtil alarmUtil = new AlarmUtil(getContext());
					alarmUtil.setAlarmFromOptions();
				} else if (key.equals(getString(R.string.preferences_general_theme_key))) {
					MainActivity_.intent(getContext()).flags(FLAG_ACTIVITY_CLEAR_TOP).start();
				} else if (key.equals(getString(R.string.preferences_general_exclude_system_apps_key))) {
					mBus.post(new UpdateInstalledAppsEvent());
				}
			} catch (IllegalStateException ignored) {

			}
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////