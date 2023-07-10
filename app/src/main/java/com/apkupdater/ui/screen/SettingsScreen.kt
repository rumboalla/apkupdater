package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apkupdater.R
import com.apkupdater.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) = Column {
	SettingsTopBar()
	SliderSetting(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar() = TopAppBar(
	title = { Text(stringResource(R.string.tab_settings)) }
)

// TODO: Do this properly
@Composable
fun SliderSetting(viewModel: SettingsViewModel) = Box(
	Modifier
		.fillMaxWidth()
		.height(60.dp)
		.padding(8.dp)
) {
	var position by remember { mutableStateOf(viewModel.getPortraitColumns().toFloat()) }
	Text("Number of columns", Modifier.align(Alignment.CenterStart))
	Row(Modifier.align(Alignment.CenterEnd)) {
		Text("${position.toInt()}",
			Modifier
				.align(CenterVertically)
				.padding(8.dp))
		Slider(
			value = position,
			valueRange = 1f..4f,
			steps = 2,
			onValueChange = {
				position = it
				viewModel.setPortraitColumns(it.toInt())
			},
			modifier = Modifier.width(150.dp)
		)
	}
}
