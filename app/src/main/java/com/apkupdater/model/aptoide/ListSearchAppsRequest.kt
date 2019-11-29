package com.apkupdater.model.aptoide

data class ListSearchAppsRequest(
	val query: String = "",
	val limit: String = "10",
	val not_apk_tags: String = "alpha,beta"
)