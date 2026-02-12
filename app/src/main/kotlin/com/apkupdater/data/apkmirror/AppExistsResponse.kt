package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

@Serializable
data class AppExistsResponse(
	val data: List<AppExistsResponseData> = emptyList(),
	val headers: AppExistsResponseHeaders? = null,
	val status: Int? = null
)
