package com.apkupdater.model.apkmirror

data class AppExistsResponse(
	val data: List<AppExistsResponseData> = emptyList(),
	val headers: AppExistsResponseHeaders? = null,
	val status: Int? = null
)