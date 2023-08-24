package com.apkupdater.data.aptoide


data class ListAppsUpdatesRequest(
    val apks_data: List<ApksData>,
    val q: String,
    val not_apk_tags: String = "alpha,beta",
    val store_ids: List<Long>? = listOf(15L, 711454L)
)
