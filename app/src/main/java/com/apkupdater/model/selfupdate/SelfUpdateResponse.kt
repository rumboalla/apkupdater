package com.apkupdater.model.selfupdate

data class SelfUpdateResponse(
	val version: Int = 0,
	val apk: String = "",
	val changelog: String = ""
)