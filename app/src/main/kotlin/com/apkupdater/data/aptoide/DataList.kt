package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable

@Serializable
data class DataList(val list: List<App> = emptyList())
