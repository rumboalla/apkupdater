package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.ui.component.DefaultErrorScreen
import com.apkupdater.ui.component.DefaultLoadingScreen
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.UpdateItem
import com.apkupdater.viewmodel.BottomBarViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun UpdatesScreen(
	barViewModel: BottomBarViewModel,
	viewModel: UpdatesViewModel = koinViewModel()
) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		barViewModel.changeUpdatesBadge("")
		UpdatesScreenLoading()
	}.onError {
		barViewModel.changeUpdatesBadge("!")
		UpdatesScreenError()
	}.onSuccess {
		barViewModel.changeUpdatesBadge(it.updates.count().toString())
		UpdatesScreenSuccess(viewModel, it.updates)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesTopBar() = TopAppBar(
	title = {
		Text(stringResource(R.string.tab_updates))
	}
)

@Composable
fun UpdatesScreenLoading() = DefaultLoadingScreen()

@Composable
fun UpdatesScreenError() = DefaultErrorScreen()

@Composable
fun UpdatesScreenSuccess(
	viewModel: UpdatesViewModel,
	updates: List<AppUpdate>
) = Column {
	val uriHandler = LocalUriHandler.current

	UpdatesTopBar()
	InstalledGrid {
		items(updates) { update ->
			UpdateItem(update) {
				uriHandler.openUri(update.link)
			}
		}
	}
}
