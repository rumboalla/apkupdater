package com.durcheins.apkupdater.transform

import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.durcheins.apkupdater.data.ui.AppInstalled
import com.durcheins.apkupdater.util.getSignatureSha1
import com.durcheins.apkupdater.util.getSignatureSha256
import com.durcheins.apkupdater.util.name


@Suppress("DEPRECATION")
fun PackageInfo.toAppInstalled(context: Context, ignored: List<String>) = AppInstalled(
	name(context),
	packageName,
	versionName.orEmpty(),
	if (Build.VERSION.SDK_INT >= 28) longVersionCode else versionCode.toLong(),
	iconUri(packageName, applicationInfo?.icon ?: 0),
	ignored.contains(packageName),
	getSignatureSha1(),
	getSignatureSha256()
)

fun iconUri(packageName: String, id: Int): Uri = "android.resource://$packageName/$id".toUri()
