package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.AppsUiState
import com.apkupdater.ui.component.DefaultErrorScreen
import com.apkupdater.ui.component.DefaultLoadingScreen
import com.apkupdater.ui.component.ExcludeAppStoreIcon
import com.apkupdater.ui.component.ExcludeDisabledIcon
import com.apkupdater.ui.component.ExcludeSystemIcon
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.InstalledItem
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.viewmodel.AppsViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppsScreen(
	viewModel: AppsViewModel = koinViewModel()
) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		AppsScreenLoading()
	}.onError {
		AppsScreenError()
	}.onSuccess {
		AppsScreenSuccess(viewModel, it)
	}
}

@Composable
fun AppsScreenSuccess(viewModel: AppsViewModel, state: AppsUiState.Success) = Column {
	AppsTopBar(viewModel, state)
	InstalledGrid {
		items(state.apps) {
			InstalledItem(it) { app -> viewModel.ignore(app) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTopBar(viewModel: AppsViewModel, state: AppsUiState.Success) = TopAppBar(
	title = { Text(stringResource(R.string.tab_apps)) },
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	actions = {
		IconButton(onClick = { viewModel.onSystemClick() }) {
			ExcludeSystemIcon(state.excludeSystem)
		}
		IconButton(onClick = { viewModel.onAppStoreClick() }) {
			ExcludeAppStoreIcon(state.excludeAppStore)
		}
		IconButton(onClick = { viewModel.onDisabledClick() }) {
			ExcludeDisabledIcon(state.excludeDisabled)
		}
	}
)

@Composable
fun AppsScreenLoading() = DefaultLoadingScreen()

@Composable
fun AppsScreenError() = DefaultErrorScreen()
