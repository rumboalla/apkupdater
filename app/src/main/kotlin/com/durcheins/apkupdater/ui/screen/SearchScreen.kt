package com.durcheins.apkupdater.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.durcheins.apkupdater.R
import com.durcheins.apkupdater.data.ui.SearchUiState
import com.durcheins.apkupdater.ui.component.DefaultErrorScreen
import com.durcheins.apkupdater.ui.component.InstalledGrid
import com.durcheins.apkupdater.ui.component.LoadingGrid
import com.durcheins.apkupdater.ui.component.SearchItem
import com.durcheins.apkupdater.ui.theme.statusBarColor
import com.durcheins.apkupdater.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun SearchScreen(
	viewModel: SearchViewModel = koinViewModel()
) = Column {
	SearchTopBar(viewModel)
	viewModel.state().collectAsStateWithLifecycle().value.onError {
		DefaultErrorScreen()
	}.onSuccess {
		SearchScreenSuccess(it, viewModel)
	}.onLoading {
		LoadingGrid()
	}
}

@Composable
fun SearchScreenSuccess(
	state: SearchUiState.Success,
	viewModel: SearchViewModel
) = Column {
	val uriHandler = LocalUriHandler.current
	InstalledGrid {
		items(state.updates) { update ->
			SearchItem(update) {
				viewModel.install(update, uriHandler)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(viewModel: SearchViewModel) = TopAppBar(
	title = { SearchText(viewModel) },
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	windowInsets = WindowInsets(0),
	actions = {},
	navigationIcon = {
		Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
			Icon(Icons.Filled.Search, "Tab Icon")
		}
	}
)

@Composable
fun SearchText(viewModel: SearchViewModel) = Box {
	val keyboardController = LocalSoftwareKeyboardController.current
	val focusRequester = remember { FocusRequester() }
	var value by remember { mutableStateOf("") }
	OutlinedTextField(
		value = value,
		onValueChange = { value = it },
		modifier = Modifier.fillMaxWidth().padding(0.dp).focusRequester(focusRequester),
		label = { Text(stringResource(R.string.tab_search)) },
		keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
		keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
		colors = OutlinedTextFieldDefaults.colors(
			focusedBorderColor = Color.Transparent,
			unfocusedBorderColor = Color.Transparent
		),
		maxLines = 1,
		singleLine = true
	)
	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}
	LaunchedEffect(value) {
		if (value.length >= 3) {
			delay(1000)
			viewModel.search(value)
		}
	}
}
