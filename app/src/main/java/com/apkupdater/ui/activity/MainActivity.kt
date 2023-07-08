package com.apkupdater.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.apkupdater.ui.screen.MainScreen
import com.apkupdater.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent { AppTheme { MainScreen() }  }
	}
}
