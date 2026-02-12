package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class AppExistsResponseRelease(
	val version: String = "",
	@SerialName("publish_date") val publishDate: String? = null,
	@SerialName("whats_new") val whatsNew: String? = null,
	val link: String? = null
)
