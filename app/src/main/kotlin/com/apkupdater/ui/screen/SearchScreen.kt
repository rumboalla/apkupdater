package com.apkupdater.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.prefs.Prefs
import com.apkupdater.ui.component.EmptyGrid
import com.apkupdater.ui.component.InstalledGrid
import com.apkupdater.ui.component.LoadingGrid
import com.apkupdater.ui.component.SearchItem
import com.apkupdater.ui.component.TvInstalledGrid
import com.apkupdater.ui.component.TvSearchItem
import com.apkupdater.ui.theme.statusBarColor
import com.apkupdater.util.formatBytes
import com.apkupdater.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import org.koin.compose.koinInject


@Composable
fun SearchScreen(viewModel: SearchViewModel) {
	val state by viewModel.state().collectAsStateWithLifecycle()
	when (val s = state) {
		is SearchUiState.Loading -> SearchScreenLoading(viewModel)
		is SearchUiState.Error -> SearchScreenError(viewModel, s.message)
		is SearchUiState.Success -> SearchScreenSuccess(viewModel, s)
	}
}

@Composable
fun SearchScreenLoading(viewModel: SearchViewModel) = Column {
	SearchTopBar(viewModel)
	LoadingGrid()
}

@Composable
fun SearchScreenError(viewModel: SearchViewModel, message: String?) = Column {
    SearchTopBar(viewModel)
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message ?: stringResource(R.string.something_went_wrong))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* ViewModel search is triggered by top bar */ }) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

@Composable
fun SearchScreenSuccess(
	viewModel: SearchViewModel,
	state: SearchUiState.Success
) = Column {
	val handler = LocalUriHandler.current
	val prefs: Prefs = koinInject()

	SearchTopBar(viewModel)

    val installingApps = state.updates.filter { it.isInstalling || it.error != null }
    AnimatedVisibility(
        visible = installingApps.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(12.dp)
        ) {
            Text(
                text = "Global Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            installingApps.forEach { app ->
                Column(
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = app.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        IconButton(
                            onClick = { viewModel.cancelInstall(app.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    app.error?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } ?: run {
                        val progress = if (app.total > 0) app.progress.toFloat() / app.total.toFloat() else 0f
                        val progressPercent = (progress * 100).toInt()
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            strokeCap = StrokeCap.Round
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (app.total > 0) {
                                Text(
                                    text = "${app.progress.formatBytes()} / ${app.total.formatBytes()} ($progressPercent%)\n",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

	if (prefs.androidTvUi.get()) {
		TvInstalledGrid {
			items(state.updates) { update ->
				TvSearchItem(update) { viewModel.install(update, handler) }
			}
		}
	} else {
		InstalledGrid {
			if (state.updates.isEmpty()) {
				item { EmptyGrid() }
			} else {
				items(state.updates) { update ->
					SearchItem(update) { viewModel.install(update, handler) }
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(viewModel: SearchViewModel) = TopAppBar(
	title = {
		var text by remember { mutableStateOf("") }
		val focusRequester = remember { FocusRequester() }
		val keyboard = LocalSoftwareKeyboardController.current

		OutlinedTextField(
			value = text,
			onValueChange = { text = it },
			modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
			placeholder = { Text(stringResource(R.string.tab_search)) },
			trailingIcon = {
				Box(Modifier.minimumInteractiveComponentSize().size(40.dp), Alignment.Center) {
					Icon(Icons.Filled.Search, "Search Icon")
				}
			},
			keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
			keyboardActions = KeyboardActions(onSearch = {
				viewModel.search(text)
				keyboard?.hide()
			}),
			colors = OutlinedTextFieldDefaults.colors(
				focusedBorderColor = Color.Transparent,
				unfocusedBorderColor = Color.Transparent
			),
			maxLines = 1
		)

		LaunchedEffect(Unit) {
			delay(300)
			focusRequester.requestFocus()
		}
	},
	colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.statusBarColor()),
	windowInsets = WindowInsets(0)
)
