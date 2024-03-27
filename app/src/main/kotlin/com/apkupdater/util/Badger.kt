package com.apkupdater.util

import com.apkupdater.data.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow


class Badger {

    private val badges = MutableStateFlow(mapOf(
        Screen.Apps.route to "",
        Screen.Search.route to "",
        Screen.Updates.route to "",
        Screen.Settings.route to ""
    ))

    fun flow() = badges

    fun changeSearchBadge(number: String) = changeBadge(Screen.Search.route, number)

    fun changeAppsBadge(number: String) = changeBadge(Screen.Apps.route, number)

    fun changeUpdatesBadge(number: String) = changeBadge(Screen.Updates.route, number)

    private fun changeBadge(route: String, number: String) {
        val finalNumber = if (number.toIntOrNull() == 0) "" else number
        val newBadges = badges.value.toMutableMap()
        newBadges[route] = finalNumber
        badges.value = newBadges
    }

}
