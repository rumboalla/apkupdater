package com.apkupdater.data.ui

import android.net.Uri

data class AppUpdate(
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Long,
	val source: Source,
	val iconUri: Uri = Uri.EMPTY,
	val link: String = "",
)
