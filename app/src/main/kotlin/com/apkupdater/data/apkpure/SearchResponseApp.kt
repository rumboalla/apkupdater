package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class SearchResponseApp(
    val ad: Boolean,
    val app_info: AppUpdateResponse
)
