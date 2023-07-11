package com.apkupdater.transform

import android.content.Context
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Build
import com.apkupdater.data.apkmirror.AppExistsResponseApk
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.util.getSignatureHash
import com.apkupdater.util.name


@Suppress("DEPRECATION")
fun PackageInfo.toAppInstalled(context: Context, ignored: List<String>) = AppInstalled(
	name(context),
	packageName,
	versionName.orEmpty(),
	if (Build.VERSION.SDK_INT >= 28) longVersionCode else versionCode.toLong(),
	iconUri(packageName, applicationInfo.icon),
	ignored.contains(packageName),
	getSignatureHash()
)

fun AppExistsResponseApk.toAppUpdate(app: AppInstalled) = AppUpdate(
	app.name,
	app.packageName,
	"?",
	versionCode,
	app.iconUri,
	"https://www.apkmirror.com/$link"
)

fun iconUri(packageName: String, id: Int): Uri = Uri.parse("android.resource://$packageName/$id")

