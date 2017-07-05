package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.apkupdater.R;
import com.apkupdater.activity.MainActivity_;
import com.apkupdater.dialog.OwnPlayAccountDialog;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.model.Constants;
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

    @Override
    public void onActivityResult(
        int requestCode,
        int resultCode,
        Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	SharedPreferences.OnSharedPreferenceChangeListener mChanges = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
			SharedPreferences prefs,
			String key
		) {
			try {
				if (key.equals(getString(R.string.preferences_general_alarm_key)) || key.equals(getString(R.string.preferences_general_update_hour_key))) {
					// Change alarm
					AlarmUtil alarmUtil = new AlarmUtil(getContext());
					alarmUtil.setAlarmFromOptions();
				} else if (key.equals(getString(R.string.preferences_general_theme_key))) {
					// Change theme
					MainActivity_.intent(getContext()).flags(FLAG_ACTIVITY_CLEAR_TOP).start();
				} else if (key.equals(getString(R.string.preferences_general_exclude_system_apps_key)) ||
					key.equals(getString(R.string.preferences_general_exclude_disabled_apps_key))
				) {
					// Update list of apps
					mBus.post(new UpdateInstalledAppsEvent());
				} else if (key.equals(getString(R.string.preferences_play_own_account_key))) {
                    if (prefs.getBoolean(getString(R.string.preferences_play_own_account_key), false)) {
                        OwnPlayAccountDialog d = new OwnPlayAccountDialog();
                        d.setTargetFragment(SettingsFragment.this, Constants.OwnPlayAccountRequestCode);
                        d.show(getFragmentManager(), getTag());
                    }
                }
			} catch (IllegalStateException ignored) {

			}
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////