package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class AppUpdateResponseAsset(
    val type: String,
    val url: String
)
