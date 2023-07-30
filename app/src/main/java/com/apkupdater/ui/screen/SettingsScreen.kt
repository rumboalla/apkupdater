package com.apkupdater.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apkupdater.R
import com.apkupdater.ui.component.DropDownSetting
import com.apkupdater.ui.component.SliderSetting
import com.apkupdater.ui.component.SwitchSetting
import com.apkupdater.ui.component.MediumTitle
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) = Column {
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

	SettingsTopBar()

	LazyColumn {
		item {
			MediumTitle(stringResource(R.string.settings_ui), Modifier.padding(horizontal = 8.dp))
			SwitchSetting(
				getValue = { viewModel.getAndroidTvUi() },
				setValue = { viewModel.setAndroidTvUi(it) },
				text = stringResource(R.string.settings_android_tv_ui)
			)
			SliderSetting(
				{ viewModel.getPortraitColumns().toFloat() },
				{ viewModel.setPortraitColumns(it.toInt()) },
				stringResource(R.string.settings_portrait_columns),
				1f..4f,
				2
			)
			SliderSetting(
				{ viewModel.getLandscapeColumns().toFloat() },
				{ viewModel.setLandscapeColumns(it.toInt()) },
				stringResource(R.string.settings_landscape_columns),
				1f..8f,
				6
			)
		}

		item {
			MediumTitle(stringResource(R.string.settings_sources), Modifier.padding(horizontal = 8.dp))
			SwitchSetting(
				{ viewModel.getUseGitHub() },
				{ viewModel.setUseGitHub(it) },
				stringResource(R.string.source_github)
			)
			SwitchSetting(
				{ viewModel.getUseApkMirror() },
				{ viewModel.setUseApkMirror(it) },
				stringResource(R.string.source_apkmirror)
			)
			SwitchSetting(
				{ viewModel.getUseFdroid() },
				{ viewModel.setUseFdroid(it) },
				stringResource(R.string.source_fdroid)
			)
		}

		item {
			MediumTitle(stringResource(R.string.settings_options), Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
			SwitchSetting(
				{ viewModel.getIgnoreAlpha() },
				{ viewModel.setIgnoreAlpha(it) },
				stringResource(R.string.ignore_alpha)
			)
			SwitchSetting(
				{ viewModel.getIgnoreBeta() },
				{ viewModel.setIgnoreBeta(it) },
				stringResource(R.string.ignore_beta)
			)
		}

		item {
			MediumTitle(stringResource(R.string.settings_alarm), Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
			SwitchSetting(
				getValue = { viewModel.getEnableAlarm() },
				setValue = { viewModel.setEnableAlarm(it, launcher) },
				text = stringResource(R.string.settings_alarm)
			)
			SliderSetting(
				getValue = { viewModel.getAlarmHour().toFloat() },
				setValue = { viewModel.setAlarmHour(it.toInt()) },
				text = stringResource(R.string.settings_hour),
				valueRange = 0f..23f,
				steps = 23
			)
			DropDownSetting(
				"Frequency",
				listOf(
					stringResource(R.string.settings_alarm_daily),
					stringResource(R.string.settings_alarm_3day),
					stringResource(R.string.settings_alarm_weekly)
				),
				{ viewModel.getAlarmFrequency() },
				{ viewModel.setAlarmFrequency(it) }
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() = TopAppBar(
	title = { Text(stringResource(R.string.tab_settings)) },
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
)
