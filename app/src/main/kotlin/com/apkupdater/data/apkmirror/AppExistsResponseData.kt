package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

@Serializable
data class AppExistsResponseData(
	val pname: String = "",
	val exists: Boolean? = null,
	val developer: AppExistsResponseDeveloper? = null,
	val app: AppExistsResponseApp = AppExistsResponseApp(),
	val release: AppExistsResponseRelease = AppExistsResponseRelease(),
	val apks: List<AppExistsResponseApk> = emptyList()
)
