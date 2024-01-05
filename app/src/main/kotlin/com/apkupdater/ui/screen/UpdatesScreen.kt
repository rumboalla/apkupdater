package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.foundation.lazy.grid.items
import com.apkupdater.R
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.prefs.Prefs
import com.apkupdater.ui.component.DefaultErrorScreen
import com.apkupdater.ui.component.EmptyGrid
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.LoadingGrid
import com.apkupdater.ui.component.RefreshIcon
import com.apkupdater.ui.component.TvInstalledGrid
import com.apkupdater.ui.component.TvUpdateItem
import com.apkupdater.ui.component.UpdateItem
import com.apkupdater.ui.theme.AppTheme
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import org.koin.androidx.compose.get


@Composable
fun UpdatesScreen(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel
) {
	updatesViewModel.state().collectAsStateWithLifecycle().value.onLoading {
		UpdatesScreenLoading()
	}.onError {
		UpdatesScreenError()
	}.onSuccess {
		UpdatesScreenSuccess(
			mainViewModel = mainViewModel,
			updatesViewModel = updatesViewModel,
			updates = it.updates
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesTopBar(onRefresh: () -> Unit) = TopAppBar(
	title = {
		Text(stringResource(R.string.tab_updates))
	},
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	actions = {
		IconButton(onClick = onRefresh) {
			RefreshIcon(stringResource(R.string.refresh_updates))
		}
	},
	navigationIcon = {
		Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
			Icon(Icons.Filled.ThumbUp, "Tab Icon")
		}
	}
)

@Preview
@Composable
fun UpdatesTopBarPreview() {
    AppTheme(darkTheme = false) {
        UpdatesTopBar { }
    }
}

@Composable
fun UpdatesScreenLoading() = Column {
	UpdatesTopBar {}
	LoadingGrid()
}

@Composable
fun UpdatesScreenError() = DefaultErrorScreen()

@Composable
fun UpdatesScreenSuccess(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel,
	updates: List<AppUpdate>
) = Column {
	UpdatesTopBar {
		mainViewModel.refresh(updatesViewModel = updatesViewModel)
	}

	val handler = LocalUriHandler.current
	val tv = get<Prefs>().androidTvUi.get()

	when {
		updates.isEmpty() -> EmptyGrid()
		tv -> TvGrid(updatesViewModel, updates, handler)
		!tv -> Grid(updatesViewModel, updates, handler)
	}
}

@Composable
fun TvGrid(
	viewModel: UpdatesViewModel,
	updates: List<AppUpdate>,
	handler: UriHandler
) = TvInstalledGrid {
	items(updates) { update ->
		TvUpdateItem(
			update,
			{ viewModel.install(update, handler) },
			{ viewModel.ignoreVersion(update.id)}
		)
	}
}

@Composable
fun Grid(
	viewModel: UpdatesViewModel,
	updates: List<AppUpdate>,
	handler: UriHandler
) = InstalledGrid {
	items(updates) { update ->
		UpdateItem(update) {
			viewModel.install(update, handler)
		}
	}
}
