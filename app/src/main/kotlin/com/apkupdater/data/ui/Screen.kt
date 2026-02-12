package com.apkupdater.data.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.apkupdater.R
import com.apkupdater.ui.icons.ThumbUpFilled
import com.apkupdater.ui.icons.ThumbUpOutlined

sealed class Screen(
	val route: String,
	@StringRes val resourceId: Int,
	val icon: ImageVector,
	val iconSelected: ImageVector
) {
	data object Apps : Screen("apps", R.string.tab_apps, Icons.Outlined.Home, Icons.Filled.Home)
	data object Search : Screen("search", R.string.tab_search, Icons.Outlined.Search, Icons.Filled.Search)
	data object Updates : Screen("updates", R.string.tab_updates, ThumbUpOutlined, ThumbUpFilled)
	data object Settings : Screen("settings", R.string.tab_settings, Icons.Outlined.Settings, Icons.Filled.Settings)
}
