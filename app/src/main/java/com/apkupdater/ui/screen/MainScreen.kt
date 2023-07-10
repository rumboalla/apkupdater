package com.apkupdater.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apkupdater.data.ui.Screen
import com.apkupdater.ui.component.BadgeText
import com.apkupdater.viewmodel.BottomBarViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun MainScreen(viewModel: BottomBarViewModel = koinViewModel()) {
	val navController = rememberNavController()
	Scaffold(bottomBar = { BottomBar(navController, viewModel) }) { padding ->
		NavHost(navController, padding, viewModel)
	}
}

@Composable
fun BottomBar(navController: NavController, viewModel: BottomBarViewModel) = BottomAppBar {
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
	viewModel: BottomBarViewModel
) = NavHost(
	navController = navController,
	startDestination = Screen.Apps.route,
	modifier = Modifier.padding(padding)
) {
	composable(Screen.Apps.route) { AppsScreen(viewModel) }
	composable(Screen.Search.route) { SearchScreen() }
	composable(Screen.Updates.route) { UpdatesScreen(viewModel) }
	composable(Screen.Settings.route) { SettingsScreen() }
}

fun navigateTo(navController: NavController, route: String) = navController.navigate(route) {
	popUpTo(navController.graph.findStartDestination().id) { saveState = true }
	launchSingleTop = true
	restoreState = true
}
