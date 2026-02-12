package com.apkupdater.data.apkmirror

import kotlinx.serialization.Serializable

import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Link
import kotlinx.serialization.SerialName

@Serializable
data class AppExistsResponseApk(
	@SerialName("version_code") val versionCode: Long = 0,
	val link: String = "",
	@SerialName("publish_date") val publishDate: String? = null,
	val arches: List<String> = emptyList(),
	val dpis: List<String>? = null,
	val minapi: String = "0",
	val description: String? = null,
	val capabilities: List<String>? = null,
	@SerialName("signatures-sha1")
	val signaturesSha1: List<String>? = emptyList(),
	@SerialName("signatures-sha256")
	val signaturesSha256: List<String>? = emptyList()
)

fun AppExistsResponseApk.toAppUpdate(app: AppInstalled, release: AppExistsResponseRelease) = AppUpdate(
	app.name,
	app.packageName,
	release.version,
	app.version,
	versionCode,
	app.versionCode,
	ApkMirrorSource,
	app.iconUri,
	Link.Url("https://www.apkmirror.com$link"),
	release.whatsNew.orEmpty()
)
