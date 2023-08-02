package com.apkupdater.data.aptoide


data class ListSearchAppsRequest(
	val query: String = "",
	val limit: String = "10",
	val notApkTags: String = "alpha,beta",
	val q: String
)
