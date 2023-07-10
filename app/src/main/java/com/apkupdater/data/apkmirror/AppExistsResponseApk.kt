package com.apkupdater.data.apkmirror

import com.google.gson.annotations.SerializedName

data class AppExistsResponseApk(
	@SerializedName("version_code") val versionCode: Long = 0,
	val link: String = "",
	@SerializedName("publish_date") val publishDate: String? = null,
	val arches: List<String> = emptyList(),
	val dpis: List<String>? = null,
	val minapi: String = "0",
	val description: String? = null,
	val capabilities: List<String>? = null
)
