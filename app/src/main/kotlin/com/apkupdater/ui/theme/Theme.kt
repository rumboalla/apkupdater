package com.apkupdater.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.apkupdater.util.isDark


@Composable
fun AppTheme(
	darkTheme: Boolean,
	dynamicColor: Boolean = true,
	content: @Composable () -> Unit
) {
	val colorScheme = when {
		dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
			val context = LocalContext.current
			if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
		}
		darkTheme -> darkColorScheme()
		else -> lightColorScheme()
	}

	val view = LocalView.current
	if (!view.isInEditMode) {
		SideEffect {
			val activity = view.context as Activity

			// Set Navigation Bar color
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				activity.window.navigationBarColor = colorScheme.statusBarColor().toArgb()
				WindowCompat.getInsetsController(
					activity.window,
					view
				).isAppearanceLightNavigationBars = !darkTheme
			}

			// Set Status Bar color
			activity.window.statusBarColor = colorScheme.statusBarColor().toArgb()
			WindowCompat.getInsetsController(
				activity.window,
				view
			).isAppearanceLightStatusBars = !darkTheme
		}
	}

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(16.dp),
        medium = RoundedCornerShape(24.dp),
        large = RoundedCornerShape(32.dp),
        extraLarge = RoundedCornerShape(48.dp)
    )

    // MotionScheme is likely not available in the alpha04 or has different API.
    // I'll skip it to ensure build stability as I cannot verify the API.

	MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        content = content
    )
}

fun ColorScheme.statusBarColor() = surfaceColorAtElevation(3.dp)

fun isDarkTheme(theme: Int): Boolean {
	if (theme == 1) return true
	if (theme == 2) return false
	return isDark()
}
