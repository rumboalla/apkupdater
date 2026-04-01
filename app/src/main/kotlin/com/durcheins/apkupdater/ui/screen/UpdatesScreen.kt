package com.durcheins.apkupdater.ui.screen

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.ui.AppUpdate
import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.ui.component.DefaultErrorScreen
import com.durcheins.apkupdater.ui.component.DownloadIcon
import com.durcheins.apkupdater.ui.component.EmptyGrid
import com.durcheins.apkupdater.ui.component.InstalledGrid
import com.durcheins.apkupdater.ui.component.LoadingGrid
import com.durcheins.apkupdater.ui.component.RefreshIcon
import com.durcheins.apkupdater.ui.component.UpdateItem
import com.durcheins.apkupdater.ui.theme.statusBarColor
import com.durcheins.apkupdater.viewmodel.UpdatesViewModel
import org.koin.compose.koinInject


@Composable
fun UpdatesScreen(viewModel: UpdatesViewModel) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		UpdatesScreenLoading(viewModel)
	}.onError {
		UpdatesScreenError()
	}.onSuccess {
		UpdatesScreenSuccess(viewModel, it.updates)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesTopBar(viewModel: UpdatesViewModel) = TopAppBar(
	title = {
		Text(stringResource(R.string.tab_updates))
	},
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	windowInsets = WindowInsets(0),
	actions = {
		if(koinInject<Prefs>().newInstaller.get()) {
			IconButton(onClick = { viewModel.installAll() }) {
				DownloadIcon(stringResource(R.string.install_all))
			}
		}
		IconButton(onClick = { viewModel.refresh() }) {
			RefreshIcon(stringResource(R.string.refresh_updates))
		}
	},
	navigationIcon = {
		Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
			Icon(Icons.Filled.ThumbUp, "Tab Icon")
		}
	}
)

@Composable
fun UpdatesScreenLoading(viewModel: UpdatesViewModel) = Column {
	UpdatesTopBar(viewModel)
	LoadingGrid()
}

@Composable
fun UpdatesScreenError() = DefaultErrorScreen()

@Composable
fun UpdatesScreenSuccess(
	viewModel: UpdatesViewModel,
	updates: List<AppUpdate>
) = Column {
	val handler = LocalUriHandler.current
	UpdatesTopBar(viewModel)
	when {
		updates.isEmpty() -> EmptyGrid()
		else -> InstalledGrid {
			items(updates) { update ->
				UpdateItem(update) {
					viewModel.install(update, handler)
				}
			}
		}
	}
}
