package com.apkupdater.model.apkmirror

import com.google.gson.annotations.SerializedName

data class AppExistsResponseApk(
	@SerializedName("version_code") var versionCode: Int = 0,
	val link: String = "",
	@SerializedName("publish_date") var publishDate: String? = null,
	val arches: List<String> = emptyList(),
	val dpis: List<String>? = null,
	val minapi: String = "0",
	val description: String? = null,
	val capabilities: List<String>? = null
)

