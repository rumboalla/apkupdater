package com.apkupdater.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.apkupdater.R

sealed class Screen(
	val route: String,
	@StringRes val resourceId: Int,
	val icon: ImageVector,
	val iconSelected: ImageVector
) {
	object Apps : Screen("apps", R.string.tab_apps, Icons.Outlined.Home, Icons.Filled.Home)
	object Search : Screen("search", R.string.tab_search, Icons.Outlined.Search, Icons.Filled.Search)
	object Updates : Screen("updates", R.string.tab_updates, Icons.Outlined.ThumbUp, Icons.Filled.ThumbUp)
	object Settings : Screen("settings", R.string.tab_settings, Icons.Outlined.Settings, Icons.Filled.Settings)
}
