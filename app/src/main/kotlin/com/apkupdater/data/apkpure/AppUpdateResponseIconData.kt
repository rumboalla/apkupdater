package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class AppUpdateResponseIconData(
    val height: String,
    val width: String,
    val url: String
)
