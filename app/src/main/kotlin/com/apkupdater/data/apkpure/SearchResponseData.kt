package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class SearchResponseData(
    val data: List<SearchResponseItem>
)
