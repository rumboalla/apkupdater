package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.ui.component.DefaultErrorScreen
import com.apkupdater.ui.component.DefaultLoadingScreen
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.UpdateItem
import com.apkupdater.viewmodel.BottomBarViewModel
import com.apkupdater.viewmodel.SearchViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
	barViewModel: BottomBarViewModel,
	viewModel: SearchViewModel = koinViewModel()
) {
	viewModel.state().collectAsStateWithLifecycle().value.onLoading {
		barViewModel.changeSearchBadge("")
		SearchScreenLoading()
	}.onError {
		barViewModel.changeSearchBadge("!")
		SearchScreenError()
	}.onSuccess {
		barViewModel.changeSearchBadge(it.updates.count().toString())
		SearchScreenSuccess(viewModel, it.updates)
	}
}

@Composable
fun SearchScreenLoading() = DefaultLoadingScreen()

@Composable
fun SearchScreenError() = DefaultErrorScreen()

@Composable
fun SearchScreenSuccess(
	viewModel: SearchViewModel,
	updates: List<AppUpdate>
) = Column {
	val uriHandler = LocalUriHandler.current
	SearchTopBar(viewModel)
	InstalledGrid {
		items(updates) { update ->
			UpdateItem(update) {
				uriHandler.openUri(update.link)
			}
		}
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchTopBar(viewModel: SearchViewModel) = Box {
	val keyboardController = LocalSoftwareKeyboardController.current
	var value by remember { mutableStateOf("") }
	OutlinedTextField(
		value = value,
		onValueChange = {
			value = it
			if (it.length >= 3) viewModel.search(it)
		},
		modifier = Modifier.fillMaxWidth().padding(8.dp),
		label = { Text("Search") },
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
		keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
	)
}
