package com.durcheins.apkupdater.util

import com.durcheins.apkupdater.prefs.Prefs
import com.durcheins.apkupdater.ui.theme.isDarkTheme
import kotlinx.coroutines.flow.MutableStateFlow

class Themer(prefs: Prefs) {

    private val theme = MutableStateFlow(isDarkTheme(prefs.theme.get()))

    fun flow() = theme

    fun setTheme(v: Boolean) { theme.value = v }

}
