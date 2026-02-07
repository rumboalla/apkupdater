package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

@Serializable
data class AppExistsResponseDeveloper(
    val name: String? = null,
    val link: String? = null
)
