package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

@Serializable
data class AppExistsResponseApp(
    val name: String = "",
    val description: String? = null,
    val link: String? = null
)
