package com.apkupdater.data.fdroid

import kotlinx.serialization.Serializable


@Serializable
data class FdroidLocalized(
    val name: String = "",
    val summary: String = "",
    val whatsNew: String = ""
)
