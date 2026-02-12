package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ListAppUpdatesResponse(
    val list: List<App>,
    val info: JsonElement,
    val errors: JsonElement
)
