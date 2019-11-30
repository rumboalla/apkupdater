package com.apkupdater.fragment

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.apkupdater.R
import com.apkupdater.util.AlarmUtil
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

	private val alarmUtil: AlarmUtil by inject()

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.settings, rootKey)

		findPreference<SwitchPreferenceCompat>(getString(R.string.settings_check_for_updates_key))?.setOnPreferenceChangeListener { _, _ ->
			context?.let { alarmUtil.setupAlarm(it) }
			true
		}

		findPreference<ListPreference>(getString(R.string.settings_theme_key))?.setOnPreferenceChangeListener { _, _ ->
			activity?.recreate()
			true
		}
	}

}