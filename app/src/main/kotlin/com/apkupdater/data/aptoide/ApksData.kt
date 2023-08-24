package com.apkupdater.data.aptoide

import com.google.gson.annotations.SerializedName

data class ApksData(
	@SerializedName("package") val packageName: String = "",
	val vercode: String = "0",
	val signature: String?,
	val isEnabled: Boolean = true
)
