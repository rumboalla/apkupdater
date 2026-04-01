package com.durcheins.apkupdater.ui.screen

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.activity.compose.LocalActivity
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
import com.durcheins.apkupdater.data.ui.Screen
import com.durcheins.apkupdater.ui.component.BadgeText
import com.durcheins.apkupdater.ui.theme.AppTheme
import com.durcheins.apkupdater.util.Badger
import com.durcheins.apkupdater.util.InstallLog
import com.durcheins.apkupdater.util.SnackBar
import com.durcheins.apkupdater.util.Themer
import com.durcheins.apkupdater.viewmodel.AppsViewModel
import com.durcheins.apkupdater.viewmodel.MainViewModel
import com.durcheins.apkupdater.viewmodel.SearchViewModel
import com.durcheins.apkupdater.viewmodel.SettingsViewModel
import com.durcheins.apkupdater.viewmodel.UpdatesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.coroutines.CoroutineContext

private const val NAV_ANIM_DURATION = 300

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
	val appsViewModel: AppsViewModel = koinViewModel()
	val updatesViewModel: UpdatesViewModel = koinViewModel()
	val searchViewModel: SearchViewModel = koinViewModel()
	val settingsViewModel: SettingsViewModel = koinViewModel()

	val navController = rememberNavController()

	val isRefreshing = mainViewModel.isRefreshing.collectAsStateWithLifecycle()
	val pullToRefreshState = rememberPullToRefreshState()
	LaunchedEffect(Unit) {
		mainViewModel.refresh(appsViewModel, updatesViewModel)
	}

	val installLog = koinInject<InstallLog>()
	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
		if (it.resultCode == RESULT_CANCELED) {
			installLog.cancelCurrentInstall()
		}
	}

	CheckNotificationIntent(mainViewModel, updatesViewModel, navController, launcher)
	IntentListener(mainViewModel, updatesViewModel, navController, launcher)

	val theme = koinInject<Themer>().flow().collectAsStateWithLifecycle().value
	val snackBarHostState = handleSnackBar()

	AppTheme(theme) {
		Scaffold(
			snackbarHost = { SnackbarHost(snackBarHostState) },
			bottomBar = { BottomBar(mainViewModel, navController) }
		) { padding ->
			PullToRefreshBox(
				isRefreshing = isRefreshing.value,
				onRefresh = { mainViewModel.refresh(appsViewModel, updatesViewModel) },
				state = pullToRefreshState
			) {
				NavHost(navController, padding, mainViewModel, appsViewModel, updatesViewModel, searchViewModel, settingsViewModel)
			}
		}
	}
}

@Composable
fun handleSnackBar(): SnackbarHostState {
	val snackBarHostState = remember { SnackbarHostState() }
	koinInject<SnackBar>().flow().CollectAsEffect(Dispatchers.IO) {
		snackBarHostState.showSnackbar(it)
	}
	return snackBarHostState
}

@Composable
fun <T> Flow<T>.CollectAsEffect(
	context: CoroutineContext = Dispatchers.IO,
	block: suspend (T) -> Unit
) = LaunchedEffect(Unit) {
	onEach(block).flowOn(context).launchIn(this)
}

@Composable
fun IntentListener(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel,
	navController: NavController,
	launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
	val activity = LocalActivity.current as? ComponentActivity ?: return
	DisposableEffect(Unit) {
		val listener = Consumer<Intent> {
			mainViewModel.processIntent(it, launcher, updatesViewModel, navController)
		}
		activity.addOnNewIntentListener(listener)
		onDispose { activity.removeOnNewIntentListener(listener) }
	}
}

@Composable
fun CheckNotificationIntent(
	mainViewModel: MainViewModel,
	updatesViewModel: UpdatesViewModel,
	navController: NavController,
	launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
	val activity = LocalActivity.current as? ComponentActivity ?: return
	mainViewModel.processIntent(activity.intent, launcher, updatesViewModel, navController)
}

@Composable
fun BottomBar(mainViewModel: MainViewModel, navController: NavController) = NavigationBar {
	val badges = koinInject<Badger>().flow().collectAsStateWithLifecycle().value
	mainViewModel.screens.forEach { screen ->
		val state = navController.currentBackStackEntryAsState().value
		val selected = state?.destination?.route == screen.route
		BottomBarItem(mainViewModel, navController, screen, selected, badges[screen.route].orEmpty())
	}
}

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
	modifier = Modifier.padding(padding),
	enterTransition = {
		slideIntoContainer(
			towards = AnimatedContentTransitionScope.SlideDirection.Start,
			animationSpec = tween(NAV_ANIM_DURATION)
		) + fadeIn(tween(NAV_ANIM_DURATION))
	},
	exitTransition = {
		slideOutOfContainer(
			towards = AnimatedContentTransitionScope.SlideDirection.Start,
			animationSpec = tween(NAV_ANIM_DURATION)
		) + fadeOut(tween(NAV_ANIM_DURATION))
	},
	popEnterTransition = {
		slideIntoContainer(
			towards = AnimatedContentTransitionScope.SlideDirection.End,
			animationSpec = tween(NAV_ANIM_DURATION)
		) + fadeIn(tween(NAV_ANIM_DURATION))
	},
	popExitTransition = {
		slideOutOfContainer(
			towards = AnimatedContentTransitionScope.SlideDirection.End,
			animationSpec = tween(NAV_ANIM_DURATION)
		) + fadeOut(tween(NAV_ANIM_DURATION))
	}
) {
	composable(Screen.Apps.route) { AppsScreen(appsViewModel) }
	composable(Screen.Search.route) { SearchScreen(searchViewModel) }
	composable(Screen.Updates.route) { UpdatesScreen(updatesViewModel) }
	composable(Screen.Settings.route) { SettingsScreen(settingsViewModel) }
}
