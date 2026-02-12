package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable

@Serializable
data class File(
    val vername: String = "",
    val vercode: String = "0",
    val path: String = ""
)
