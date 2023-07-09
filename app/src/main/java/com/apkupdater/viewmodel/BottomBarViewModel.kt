package com.apkupdater.viewmodel

import androidx.lifecycle.ViewModel
import com.apkupdater.data.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow

class BottomBarViewModel : ViewModel() {

	val screens = listOf(Screen.Apps, Screen.Search, Screen.Updates, Screen.Settings)

	val badges = MutableStateFlow(mapOf(
		Screen.Apps.route to "",
		Screen.Search.route to "",
		Screen.Updates.route to "",
		Screen.Settings.route to ""
	))

	fun changeAppsBadge(number: String) = changeBadge(Screen.Apps.route, number)

	private fun changeBadge(route: String, number: String) {
		val newBadges = badges.value.toMutableMap()
		newBadges[route] = number
		badges.value = newBadges
	}

}



