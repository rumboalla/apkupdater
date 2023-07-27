package com.apkupdater.viewmodel

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.apkupdater.data.ui.Screen
import com.apkupdater.util.SessionInstaller
import com.apkupdater.util.UpdatesNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

	val screens = listOf(Screen.Apps, Screen.Search, Screen.Updates, Screen.Settings)

	val badges = MutableStateFlow(mapOf(
		Screen.Apps.route to "",
		Screen.Search.route to "",
		Screen.Updates.route to "",
		Screen.Settings.route to ""
	))

	val isRefreshing = MutableStateFlow(false)

	fun refresh(
		appsViewModel: AppsViewModel,
		updatesViewModel: UpdatesViewModel
	) = viewModelScope.launch {
		isRefreshing.value = true
		appsViewModel.refresh(false)
		updatesViewModel.refresh(false).invokeOnCompletion {
			isRefreshing.value = false
		}
	}

	fun changeSearchBadge(number: String) = changeBadge(Screen.Search.route, number)

	fun changeAppsBadge(number: String) = changeBadge(Screen.Apps.route, number)

	fun changeUpdatesBadge(number: String) = changeBadge(Screen.Updates.route, number)

	fun processIntent(
		intent: Intent,
		activity: Activity,
		updatesViewModel: UpdatesViewModel,
		navController: NavController
	) {
		when (intent.action) {
			UpdatesNotification.UpdateAction -> processUpdateIntent(navController, updatesViewModel)
			SessionInstaller.InstallAction -> processInstallIntent(intent, activity)
			else -> Log.e("processIntent", intent.toString())
		}
	}

	fun navigateTo(navController: NavController, route: String) = navController.navigate(route) {
		popUpTo(navController.graph.findStartDestination().id) { saveState = true }
		launchSingleTop = true
		restoreState = true
	}

	private fun processInstallIntent(intent: Intent, activity: Activity) {
		val status = intent.extras?.getInt(PackageInstaller.EXTRA_STATUS)
		when (status) {
			PackageInstaller.STATUS_PENDING_USER_ACTION -> {
				@Suppress("DEPRECATION") val confirmIntent = intent.extras?.get(Intent.EXTRA_INTENT) as Intent
				confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				ContextCompat.startActivity(activity, confirmIntent, null)
			}
			PackageInstaller.STATUS_SUCCESS -> {
				// TODO: Success
			}
			else -> {
				// TODO: Consider error
			}
		}
	}

	private fun processUpdateIntent(
		navController: NavController,
		updatesViewModel: UpdatesViewModel
	) {
		navigateTo(navController, Screen.Updates.route)
		updatesViewModel.refresh()
	}

	private fun changeBadge(route: String, number: String) {
		if (number.toIntOrNull() == 0) return
		val newBadges = badges.value.toMutableMap()
		newBadges[route] = number
		badges.value = newBadges
	}

}
