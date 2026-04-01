package com.durcheins.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.ui.AppsUiState
import com.durcheins.apkupdater.ui.component.DefaultErrorScreen
import com.durcheins.apkupdater.ui.component.ExcludeAppStoreIcon
import com.durcheins.apkupdater.ui.component.ExcludeDisabledIcon
import com.durcheins.apkupdater.ui.component.ExcludeSystemIcon
import com.durcheins.apkupdater.ui.component.InstalledGrid
import com.durcheins.apkupdater.ui.component.InstalledItem
import com.durcheins.apkupdater.ui.component.LoadingGrid
import com.durcheins.apkupdater.ui.theme.statusBarColor
import com.durcheins.apkupdater.viewmodel.AppsViewModel
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AppsScreen(
	viewModel: AppsViewModel = koinViewModel()
) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		AppsScreenLoading(viewModel, it)
	}.onError {
		AppsScreenError()
	}.onSuccess {
		AppsScreenSuccess(viewModel, it)
	}
}

@Composable
fun AppsScreenSuccess(viewModel: AppsViewModel, state: AppsUiState.Success) = Column {
	AppsTopBar(viewModel, state.excludeSystem, state.excludeAppStore, state.excludeDisabled)
	InstalledGrid {
		items(state.apps) {
			InstalledItem(it) { app -> viewModel.ignore(app) }
		}
	}
}

@Composable
fun AppsScreenLoading(viewModel: AppsViewModel, state: AppsUiState.Loading) = Column {
	AppsTopBar(viewModel, state.excludeSystem, state.excludeAppStore, state.excludeDisabled)
	LoadingGrid()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTopBar(
	viewModel: AppsViewModel,
	excludeSystem: Boolean,
	excludeAppStore: Boolean,
	excludeDisabled: Boolean
) = TopAppBar(
	title = { Text(stringResource(R.string.tab_apps)) },
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	windowInsets = WindowInsets(0),
	actions = {
		IconButton(onClick = { viewModel.onSystemClick() }) {
			ExcludeSystemIcon(excludeSystem)
		}
		IconButton(onClick = { viewModel.onAppStoreClick() }) {
			ExcludeAppStoreIcon(excludeAppStore)
		}
		IconButton(onClick = { viewModel.onDisabledClick() }) {
			ExcludeDisabledIcon(excludeDisabled)
		}
	},
	navigationIcon = {
		Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
			Icon(Icons.Filled.Home, "Tab Icon")
		}
	}
)

@Composable
fun AppsScreenError() = DefaultErrorScreen()
