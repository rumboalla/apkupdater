package com.apkupdater.data.apkpure

import kotlinx.serialization.Serializable


@Serializable
data class SearchResponseItem(
    val data: List<SearchResponseApp>
)
