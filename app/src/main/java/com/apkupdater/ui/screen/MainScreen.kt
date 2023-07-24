package com.apkupdater.ui.screen

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
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
import androidx.compose.material3.Text
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.util.Consumer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apkupdater.R
import com.apkupdater.data.ui.Screen
import com.apkupdater.ui.component.BadgeText
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.SettingsViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf


@Composable
fun MainScreen(mainViewModel: MainViewModel = koinViewModel()) {
	// ViewModels
	val appsViewModel: AppsViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })
	val updatesViewModel: UpdatesViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })
	val searchViewModel: SearchViewModel = koinViewModel(parameters = { parametersOf(mainViewModel) })

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

	// Check intent when cold starting from notification
	checkNotificationIntent(navController)

	// Check notification intent when hot starting
	intentListener(navController, updatesViewModel)

	Scaffold(bottomBar = { BottomBar(navController, mainViewModel) }) { padding ->
		Box(modifier = Modifier.pullRefresh(pullToRefresh)) {
			NavHost(navController, padding, appsViewModel, updatesViewModel, searchViewModel)
			PullRefreshIndicator(
				refreshing = isRefreshing.value,
				state = pullToRefresh,
				modifier = Modifier.align(Alignment.TopCenter),
				contentColor = MaterialTheme.colorScheme.primary
			)
		}
	}
}

@Composable
fun intentListener(
	navController: NavController,
	updatesViewModel: UpdatesViewModel
) = runCatching {
	val activity = LocalContext.current as ComponentActivity
	val action = stringResource(R.string.notification_update_action)
	DisposableEffect(Unit) {
		val listener = Consumer<Intent> { intent ->
			if (intent.action == action) {
				navigateTo(navController, Screen.Updates.route)
				updatesViewModel.refresh()
			}
		}
		activity.addOnNewIntentListener(listener)
		onDispose { activity.removeOnNewIntentListener(listener) }
	}
}.getOrElse {
	Log.e("MainScreen", "Error listening to intent.", it)
}

@Composable
fun checkNotificationIntent(navController: NavController) = runCatching {
	val activity = LocalContext.current as Activity
	if (activity.intent.action == stringResource(R.string.notification_update_action)) {
		navigateTo(navController, Screen.Updates.route)
	}
}.getOrElse {
	Log.e("MainScreen", "Error checking notification intent.", it)
}

@Composable
fun BottomBar(navController: NavController, viewModel: MainViewModel) = BottomAppBar {
	val badges = viewModel.badges.collectAsState().value
	viewModel.screens.forEach { screen ->
		val state = navController.currentBackStackEntryAsState().value
		val selected = state?.destination?.route  == screen.route
		BottomBarItem(navController, screen, selected, badges[screen.route].orEmpty())
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.BottomBarItem(
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
	label = { Text(stringResource(screen.resourceId)) },
	selected = selected,
	onClick = { navigateTo(navController, screen.route) }
)

@Composable
fun NavHost(
	navController: NavHostController,
	padding: PaddingValues,
	appsViewModel: AppsViewModel,
	updatesViewModel: UpdatesViewModel,
	searchViewModel: SearchViewModel,
	settingsViewModel: SettingsViewModel = koinViewModel()
) = NavHost(
	navController = navController,
	startDestination = Screen.Apps.route,
	modifier = Modifier.padding(padding)
) {
	composable(Screen.Apps.route) { AppsScreen(appsViewModel) }
	composable(Screen.Search.route) { SearchScreen(searchViewModel) }
	composable(Screen.Updates.route) { UpdatesScreen(updatesViewModel) }
	composable(Screen.Settings.route) { SettingsScreen(settingsViewModel) }
}

fun navigateTo(navController: NavController, route: String) = navController.navigate(route) {
	popUpTo(navController.graph.findStartDestination().id) { saveState = true }
	launchSingleTop = true
	restoreState = true
}
