package com.apkupdater.data.aptoide

import java.util.Collections.emptyList


data class ListAppsUpdatesRequest(
    val apks_data: List<ApksData> = emptyList(),
    val notApkTags: String = "alpha,beta",
    //val storeIds: List<Long>? = listOf(15L, 1966380L),
)
