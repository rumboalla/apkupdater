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
import com.apkupdater.ui.component.LargeTitle
import com.apkupdater.ui.component.SliderSetting
import com.apkupdater.ui.component.SwitchSetting
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) = Column {
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

	SettingsTopBar()

	LazyColumn {
		item {
			LargeTitle(stringResource(R.string.settings_ui), Modifier.padding(start = 16.dp, top = 16.dp))
			SwitchSetting(
				getValue = { viewModel.getAndroidTvUi() },
				setValue = { viewModel.setAndroidTvUi(it) },
				text = stringResource(R.string.settings_android_tv_ui),
				icon = R.drawable.ic_androidtv
			)
			SliderSetting(
				{ viewModel.getPortraitColumns().toFloat() },
				{ viewModel.setPortraitColumns(it.toInt()) },
				stringResource(R.string.settings_portrait_columns),
				1f..4f,
				2,
				R.drawable.ic_portrait
			)
			SliderSetting(
				{ viewModel.getLandscapeColumns().toFloat() },
				{ viewModel.setLandscapeColumns(it.toInt()) },
				stringResource(R.string.settings_landscape_columns),
				1f..8f,
				6,
				R.drawable.ic_landscape
			)
		}

		item {
			LargeTitle(stringResource(R.string.settings_sources), Modifier.padding(start = 16.dp, top = 16.dp))
			SwitchSetting(
				{ viewModel.getUseGitHub() },
				{ viewModel.setUseGitHub(it) },
				stringResource(R.string.source_github),
				R.drawable.ic_github
			)
			SwitchSetting(
				{ viewModel.getUseApkMirror() },
				{ viewModel.setUseApkMirror(it) },
				stringResource(R.string.source_apkmirror),
				R.drawable.ic_apkmirror
			)
			SwitchSetting(
				{ viewModel.getUseFdroid() },
				{ viewModel.setUseFdroid(it) },
				stringResource(R.string.source_fdroid),
				R.drawable.ic_fdroid
			)
			SwitchSetting(
				{ viewModel.getUseAptoide() },
				{ viewModel.setUseAptoide(it) },
				stringResource(R.string.source_aptoide),
				R.drawable.ic_aptoide
			)
		}

		item {
			LargeTitle(stringResource(R.string.settings_options), Modifier.padding(start = 16.dp, top = 16.dp))
			SwitchSetting(
				{ viewModel.getIgnoreAlpha() },
				{ viewModel.setIgnoreAlpha(it) },
				stringResource(R.string.ignore_alpha),
				R.drawable.ic_alpha
			)
			SwitchSetting(
				{ viewModel.getIgnoreBeta() },
				{ viewModel.setIgnoreBeta(it) },
				stringResource(R.string.ignore_beta),
				R.drawable.ic_beta
			)
		}

		item {
			LargeTitle(stringResource(R.string.settings_alarm), Modifier.padding(start = 16.dp, top = 16.dp))
			SwitchSetting(
				getValue = { viewModel.getEnableAlarm() },
				setValue = { viewModel.setEnableAlarm(it, launcher) },
				text = stringResource(R.string.settings_alarm),
				icon = R.drawable.ic_alarm
			)
			SliderSetting(
				getValue = { viewModel.getAlarmHour().toFloat() },
				setValue = { viewModel.setAlarmHour(it.toInt()) },
				text = stringResource(R.string.settings_hour),
				valueRange = 0f..23f,
				steps = 23,
				R.drawable.ic_hour
			)
			DropDownSetting(
				"Frequency",
				listOf(
					stringResource(R.string.settings_alarm_daily),
					stringResource(R.string.settings_alarm_3day),
					stringResource(R.string.settings_alarm_weekly)
				),
				{ viewModel.getAlarmFrequency() },
				{ viewModel.setAlarmFrequency(it) },
				R.drawable.ic_frequency
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
