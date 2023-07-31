package com.apkupdater.data.aptoide

import android.net.Uri
import androidx.core.net.toUri
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.AptoideSource
import com.google.gson.annotations.SerializedName

data class App(
	val name: String = "",
	@SerializedName("package") val packageName:String = "",
	val icon: String? = "",
	val file: File,
	val store: Store
)

fun App.toAppUpdate() = AppUpdate(
	name = name,
	packageName = packageName,
	version = file.vername,
	versionCode = file.vercode.toLong(),
	source = AptoideSource,
	iconUri = icon?.toUri() ?: Uri.EMPTY,
	link = file.path
)
