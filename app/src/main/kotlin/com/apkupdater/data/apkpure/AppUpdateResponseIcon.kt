package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class AppUpdateResponseIcon(
    val original: AppUpdateResponseIconData,
    val thumbnail: AppUpdateResponseIconData
)
