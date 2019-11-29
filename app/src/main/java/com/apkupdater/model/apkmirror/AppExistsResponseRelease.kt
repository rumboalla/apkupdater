package com.apkupdater.model.apkmirror

import com.google.gson.annotations.SerializedName

data class AppExistsResponseRelease(
	val version: String = "",
	@SerializedName("publish_date") val publishDate: String? = null,
	@SerializedName("whats_new") val whatsNew: String? = null,
	val link: String? = null
)
