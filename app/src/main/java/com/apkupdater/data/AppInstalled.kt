package com.apkupdater.data

import android.net.Uri

data class AppInstalled(
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Long,
	val iconUri: Uri = Uri.EMPTY,
	val ignored: Boolean = false
)