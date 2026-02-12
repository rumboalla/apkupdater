package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable


@Serializable
data class ListSearchAppsRequest(
	val query: String = "",
	val limit: String = "10",
	val q: String,
	val not_apk_tags: String = "alpha,beta",
	val store_ids: List<Long>? = listOf(15L, 711454L)
)
