package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.UiState
import com.apkupdater.ui.component.AppItem
import com.apkupdater.ui.component.ExcludeSystemIcon
import com.apkupdater.ui.component.HugeText
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.RefreshIcon
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.BottomBarViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun AppsScreen(
	barViewModel: BottomBarViewModel,
	viewModel: AppsViewModel = koinViewModel()
) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		barViewModel.changeAppsBadge("")
		AppsScreenLoading()
	}.onError {
		barViewModel.changeAppsBadge("!")
		AppsScreenError()
	}.onSuccess {
		barViewModel.changeAppsBadge(it.apps.count().toString())
		AppsScreenSuccess(viewModel, it)
	}
}

@Composable
fun AppsScreenSuccess(viewModel: AppsViewModel, state: UiState.Success) = Column {
	AppsTopBar(viewModel, state)
	InstalledGrid {
		items(state.apps) {
			AppItem(it) { app -> viewModel.ignore(app) }
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTopBar(viewModel: AppsViewModel, state: UiState.Success) = TopAppBar(
	title = { Text(stringResource(R.string.tab_apps)) },
	colors = TopAppBarDefaults.topAppBarColors(),
	actions = {
		IconButton(onClick = { viewModel.refresh() }) {
			RefreshIcon()
		}
		IconButton(onClick = { viewModel.onSystemClick() }) {
			ExcludeSystemIcon(state.excludeSystem)
		}
	}
)

@Composable
fun AppsScreenLoading() = Box(Modifier.fillMaxSize()) {
	CircularProgressIndicator(Modifier.align(Alignment.Center))
}

@Composable
fun AppsScreenError() = Box(Modifier.fillMaxSize()) {
	HugeText(
		stringResource(R.string.something_went_wrong),
		Modifier.align(Alignment.Center),
		2
	)
}