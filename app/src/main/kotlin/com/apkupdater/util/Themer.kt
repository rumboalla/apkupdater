package com.apkupdater.util

import com.apkupdater.prefs.Prefs
import com.apkupdater.ui.theme.isDarkTheme
import kotlinx.coroutines.flow.MutableStateFlow

class Themer(prefs: Prefs) {

    private val theme = MutableStateFlow(isDarkTheme(prefs.theme.get()))

    fun flow() = theme

    fun setTheme(v: Boolean) { theme.value = v }

}
