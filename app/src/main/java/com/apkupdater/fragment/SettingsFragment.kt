package com.apkupdater.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreferenceCompat
import com.apkupdater.R
import com.apkupdater.util.app.AlarmUtil
import com.apkupdater.viewmodel.MainViewModel
import eu.chainfire.libsuperuser.Shell
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SettingsFragment : PreferenceFragmentCompat() {

	private val mainViewModel: MainViewModel by sharedViewModel()
	private val alarmUtil: AlarmUtil by inject()

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings, rootKey)

		findPreference<ListPreference>(getString(R.string.settings_check_for_updates_key))?.setOnPreferenceChangeListener { _, _ ->
			context?.let { alarmUtil.setupAlarm(it) }
			true
		}

		findPreference<SeekBarPreference>(getString(R.string.settings_update_hour_key))?.setOnPreferenceChangeListener { _, _ ->
			context?.let { alarmUtil.setupAlarm(it) }
			true
		}

		findPreference<ListPreference>(getString(R.string.settings_theme_key))?.setOnPreferenceChangeListener { _, _ ->
			activity?.recreate()
			true
		}

		findPreference<SwitchPreferenceCompat>(getString(R.string.settings_root_install_key))?.setOnPreferenceChangeListener { _, _ ->
			if (Shell.SU.available()) {
				true
			} else {
				mainViewModel.snackbar.postValue("Root not available.")
				false
			}
		}
	}

}