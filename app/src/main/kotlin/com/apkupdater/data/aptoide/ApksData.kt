package com.apkupdater.data.aptoide

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class ApksData(
	@SerialName("package") val packageName: String = "",
	val vercode: String = "0",
	val signature: String?,
	val isEnabled: Boolean = true
)
