package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

@Serializable
data class AppExistsRequest(
    val pnames: List<String>,
    val exclude: List<String> = listOf("alpha", "beta")
)
