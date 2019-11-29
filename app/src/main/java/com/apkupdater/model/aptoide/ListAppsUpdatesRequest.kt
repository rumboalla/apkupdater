package com.apkupdater.model.aptoide

var aptoideFilters = ""

data class ListAppsUpdatesRequest(
	val apks_data: List<ApksData> = emptyList(),
	val not_apk_tags: String = "alpha,beta",
	val aaid: String? = null,
	val storeIds: List<Long>? = null,
	val notPackageTags: String? = null,
	val q: String = aptoideFilters
)