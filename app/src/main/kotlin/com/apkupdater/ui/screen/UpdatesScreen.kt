package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
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
import com.apkupdater.ui.component.TvGroupedUpdateItem
import com.apkupdater.ui.component.TvInstalledGrid
import com.apkupdater.ui.component.UpdateItem
import com.apkupdater.ui.icons.ThumbUpFilled
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.viewmodel.UpdatesViewModel
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
fun UpdatesTopBar(
	viewModel: UpdatesViewModel,
	loading: Boolean = false
) = TopAppBar(
	title = {
		Text(stringResource(R.string.tab_updates))
	},
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	actions = {
		IconButton(
			onClick = { viewModel.refresh() },
			enabled = !loading
		) {
			RefreshIcon(stringResource(R.string.refresh_updates))
		}
	},
	navigationIcon = {
		Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
			Icon(ThumbUpFilled, "Tab Icon")
		}
	}
)

@Composable
fun UpdatesScreenLoading(viewModel: UpdatesViewModel) = Column {
	UpdatesTopBar(viewModel, loading = true)
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
	val tv = koinInject<Prefs>().androidTvUi.get()

	UpdatesTopBar(viewModel)

	when {
		updates.isEmpty() -> EmptyGrid()
		tv -> TvGrid(viewModel, updates, handler)
		!tv -> Grid(viewModel, updates, handler)
	}
}

@Composable
fun TvGrid(
	viewModel: UpdatesViewModel,
	updates: List<AppUpdate>,
	handler: UriHandler
) = TvInstalledGrid {
	items(updates.groupBy { it.packageName }.values.toList()) { updatesForPackage ->
		TvGroupedUpdateItem(
			updatesForPackage,
			{ viewModel.install(it, handler) },
			{ viewModel.ignoreVersion(it) }
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
