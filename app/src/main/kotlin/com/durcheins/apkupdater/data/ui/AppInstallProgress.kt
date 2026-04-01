package com.durcheins.apkupdater.data.ui

data class AppInstallProgress(
    val id: Int,
    val progress: Long? = null,
    val total: Long? = null,
    val speed: Long? = null  // bytes per second
)
