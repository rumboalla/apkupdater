package com.apkupdater.ui.screen

import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.util.Consumer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apkupdater.R
import com.apkupdater.data.snack.ISnack
import com.apkupdater.data.snack.InstallSnack
import com.apkupdater.data.ui.Screen
import com.apkupdater.ui.component.BadgeText
import com.apkupdater.ui.theme.AppTheme
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.SettingsViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext


@Composable
fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
	// ViewModels
	val appsViewModel: AppsViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })
	val updatesViewModel: UpdatesViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })
	val searchViewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })
	val settingsViewModel: SettingsViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })

	// Navigation
	val navController = rememberNavController()

	// Pull to refresh
	val isRefreshing = mainViewModel.isRefreshing.collectAsStateWithLifecycle()
	val pullToRefresh = rememberPullRefreshState(isRefreshing.value, {
		mainViewModel.refresh(appsViewModel, updatesViewModel)
	})
	LaunchedEffect(pullToRefresh) {
		mainViewModel.refresh(appsViewModel, updatesViewModel)
	}

	// Used to launch the install intent and get dismissal result
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		if (it.resultCode == RESULT_CANCELED) {
			mainViewModel.cancelCurrentInstall()
		}
	}

	// Check intent when cold starting from notification
	checkNotificationIntent(mainViewModel, updatesViewModel, navController, launcher)

	// Check notification intent when hot starting
	intentListener(mainViewModel, updatesViewModel, navController, launcher)

	// Theme
	val theme = mainViewModel.theme.collectAsStateWithLifecycle().value

	// SnackBar
	val context = LocalContext.current
	val snackBarHostState = remember { SnackbarHostState() }
	mainViewModel.snackBar.CollectAsEffect {
		handleSnack(context, snackBarHostState, it)
	}

	AppTheme(theme) {
		Scaffold(
			snackbarHost = { SnackbarHost(snackBarHostState) },
			bottomBar = { BottomBar(mainViewModel, navController) }
		) { padding ->
			Box(modifier = Modifier.pullRefresh(pullToRefresh)) {
				NavHost(navController, padding, mainViewModel, appsViewModel, updatesViewModel, searchViewModel, settingsViewModel)
				PullRefreshIndicator(
					refreshing = isRefreshing.value,
					state = pullToRefresh,
					modifier = Modifier.align(Alignment.TopCenter),
					contentColor = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

suspend fun handleSnack(
	context: Context,
	snackbarHostState: SnackbarHostState,
	snack: ISnack
) {
	when (snack) {
		is InstallSnack -> {
			val message = if (snack.success) R.string.install_success else R.string.install_failure
			snackbarHostState.showSnackbar(
				context.getString(message, snack.name),
				null,
				true,
				SnackbarDuration.Short
			)
		}
		else -> Log.e("MainScreen", "Invalid snack type.")
	}
}

@Composable
fun <T> Flow<T>.CollectAsEffect(
	context: CoroutineContext = Dispatchers.IO,
	block: suspend (T) -> Unit
) {
	LaunchedEffect(Unit) {
		onEach(block).flowOn(context).launchIn(this)
	}
}

@Composable
fun intentListener(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel,
	navController: NavController,
	launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) = runCatching {
	val activity = LocalContext.current as ComponentActivity
	DisposableEffect(Unit) {
		val listener = Consumer<Intent> {
			mainViewModel.processIntent(it, launcher, updatesViewModel, navController)
		}
		activity.addOnNewIntentListener(listener)
		onDispose { activity.removeOnNewIntentListener(listener) }
	}
}.getOrElse {
	Log.e("MainScreen", "Error listening to intent.", it)
}

@Composable
fun checkNotificationIntent(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel,
	navController: NavController,
	launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) = runCatching {
	val activity = LocalContext.current as ComponentActivity
	mainViewModel.processIntent(activity.intent, launcher, updatesViewModel, navController)
}.getOrElse {
	Log.e("MainScreen", "Error checking notification intent.", it)
}

@Composable
fun BottomBar(mainViewModel: MainViewModel, navController: NavController) = BottomAppBar {
	val badges = mainViewModel.badges.collectAsState().value
	mainViewModel.screens.forEach { screen ->
		val state = navController.currentBackStackEntryAsState().value
		val selected = state?.destination?.route  == screen.route
		BottomBarItem(mainViewModel, navController, screen, selected, badges[screen.route].orEmpty())
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.BottomBarItem(
	mainViewModel: MainViewModel,
    navController: NavController,
    screen: Screen,
    selected: Boolean,
    badge: String
) = NavigationBarItem(
	icon = {
		BadgedBox({ BadgeText(badge) }) {
			Icon(if (selected) screen.iconSelected else screen.icon, contentDescription = null)
		}
   	},
	label = {
		Text(
			stringResource(screen.resourceId),
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	},
	selected = selected,
	onClick = { mainViewModel.navigateTo(navController, screen.route) }
)

@Composable
fun NavHost(
	navController: NavHostController,
	padding: PaddingValues,
	mainViewModel: MainViewModel,
	appsViewModel: AppsViewModel,
	updatesViewModel: UpdatesViewModel,
	searchViewModel: SearchViewModel,
	settingsViewModel: SettingsViewModel
) = NavHost(
	navController = navController,
	startDestination = mainViewModel.getLastRoute(),
	modifier = Modifier.padding(padding)
) {
	composable(Screen.Apps.route) { AppsScreen(appsViewModel) }
	composable(Screen.Search.route) { SearchScreen(searchViewModel) }
	composable(Screen.Updates.route) { UpdatesScreen(updatesViewModel) }
	composable(Screen.Settings.route) { SettingsScreen(settingsViewModel) }
}
