package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ListSearchAppsResponse(
    val datalist: DataList,
    val info: JsonElement,
    val errors: JsonElement
)
