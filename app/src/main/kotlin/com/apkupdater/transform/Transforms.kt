package com.apkupdater.transform

import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.util.getSignatureSha1
import com.apkupdater.util.getSignatureSha256
import com.apkupdater.util.name


@Suppress("DEPRECATION")
fun PackageInfo.toAppInstalled(context: Context, ignored: List<String>) = AppInstalled(
	name(context),
	packageName,
	versionName.orEmpty(),
	if (Build.VERSION.SDK_INT >= 28) longVersionCode else versionCode.toLong(),
	iconUri(packageName, applicationInfo.icon),
	ignored.contains(packageName),
	getSignatureSha1(),
	getSignatureSha256()
)

fun iconUri(packageName: String, id: Int): Uri = Uri.parse("android.resource://$packageName/$id")
