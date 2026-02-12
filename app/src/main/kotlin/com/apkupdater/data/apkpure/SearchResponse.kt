package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class SearchResponse(
    val ad_priority: Int,
    val data: SearchResponseData
)
