package com.apkupdater.data.ui

import android.content.Context
import android.net.Uri
import com.apkupdater.data.aptoide.ApksData
import com.apkupdater.util.UriSerializer
import com.apkupdater.util.getSignatureSha1
import com.apkupdater.util.toSha1Aptoide
import kotlinx.serialization.Serializable

@Serializable
data class AppInstalled(
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Long,
	@Serializable(with = UriSerializer::class)
	val iconUri: Uri = Uri.EMPTY,
	val ignored: Boolean = false
)

fun List<AppInstalled>.getApp(packageName: String) = find { packageName == it.packageName }

fun List<AppInstalled>.getVersionCode(packageName: String) = getApp(packageName)
	?.versionCode
	?: 0L

fun List<AppInstalled>.getVersion(packageName: String) = getApp(packageName)
	?.version
	?: ""

fun List<AppInstalled>.getPackageNames() = filter { !it.ignored }.map { it.packageName }

fun AppInstalled.toApksData(context: Context) = ApksData(
	packageName = packageName,
	vercode = versionCode.toString(),
	signature = context.getSignatureSha1(packageName).toSha1Aptoide(),
	isEnabled = true
)
