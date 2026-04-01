package com.durcheins.apkupdater.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat


@Composable
fun AppTheme(
	darkTheme: Boolean,
	content: @Composable () -> Unit
) {
	// Dynamic Color = Material You: nutzt Farben vom Hintergrundbild des Pixel
	val colorScheme = if (darkTheme) {
		dynamicDarkColorScheme(LocalContext.current)
	} else {
		dynamicLightColorScheme(LocalContext.current)
	}

	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val activity = view.context as Activity
			val insetsController = WindowCompat.getInsetsController(activity.window, view)
			insetsController.isAppearanceLightStatusBars = !darkTheme
			insetsController.isAppearanceLightNavigationBars = !darkTheme
		}
	}

	MaterialTheme(
		colorScheme = colorScheme,
		content = content
	)
}

fun ColorScheme.statusBarColor() = surfaceColorAtElevation(3.dp)

fun isDarkTheme(theme: Int): Boolean {
	if (theme == 1) return true
	if (theme == 2) return false
	return isSystemInDarkTheme()
}
