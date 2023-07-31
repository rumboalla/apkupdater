package com.apkupdater.data.ui

import android.net.Uri
import com.apkupdater.data.aptoide.ApksData
import com.apkupdater.util.toSha1Aptoide

data class AppInstalled(
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Long,
	val iconUri: Uri = Uri.EMPTY,
	val ignored: Boolean = false,
	val signature: String = ""
)

fun List<AppInstalled>.getApp(packageName: String) = find { packageName == it.packageName }

fun List<AppInstalled>.getVersionCode(packageName: String) = getApp(packageName)
	?.versionCode
	?: 0L

fun List<AppInstalled>.getSignature(packageName: String) = getApp(packageName)
	?.signature
	.orEmpty()

fun List<AppInstalled>.getPackageNames() = filter { !it.ignored }.map { it.packageName }

fun AppInstalled.toApksData() = ApksData(
	packageName = packageName,
	vercode = versionCode.toString(),
	signature = signature.toSha1Aptoide(),
	isEnabled = true
)
