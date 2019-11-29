package com.apkupdater.model.aptoide

import com.google.gson.annotations.SerializedName

data class ApksData(
	@SerializedName("package") val packageName: String = "",
	val vercode: String = "0",
	val signature: String?
)