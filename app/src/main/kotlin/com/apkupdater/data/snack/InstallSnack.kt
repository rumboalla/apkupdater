package com.apkupdater.data.snack

import androidx.annotation.StringRes

data class InstallSnack(val success: Boolean, val name: String): ISnack

data class TextSnack(val message: String): ISnack

data class TextIdSnack(@StringRes val id: Int): ISnack
