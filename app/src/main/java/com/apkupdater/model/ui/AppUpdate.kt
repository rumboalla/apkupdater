package com.apkupdater.model.ui

import android.content.Context
import android.content.pm.PackageInfo
import com.apkupdater.model.apkmirror.AppExistsResponseApk
import com.apkupdater.model.apkmirror.AppExistsResponseData
import com.apkupdater.model.aptoide.App
import com.apkupdater.util.adapter.Id
import com.apkupdater.util.crc.crc16
import com.apkupdater.util.name
import kotlin.random.Random

data class AppUpdate(
	val name: String = "",
	val packageName: String = "",
	val version: String = "",
	val versionCode: Int = 0,
	val oldVersion: String = "",
	val oldCode: Int = 0,
	val url: String = "",
	val source: Int = 0,
	override val id: Int = crc16(packageName + versionCode + source),
	var loading: Boolean = false,
	val ad: Int = Random.nextInt(10)
): Id {
	companion object {

		fun from(context: Context, info: PackageInfo, app: App, source: Int): AppUpdate =
			AppUpdate(
				info.name(context),
				app.packageName,
				app.file.vername,
				app.file.vercode.toInt(),
				info.versionName ?: "null",
				info.versionCode,
				app.file.path,
				source
			)

		fun from(app: AppInstalled, data: AppExistsResponseData, apk: AppExistsResponseApk, url: String, source: Int): AppUpdate =
			AppUpdate(
				app.name,
				data.pname,
				data.release.version,
				apk.versionCode,
				app.version,
				app.versionCode,
				url,
				source
			)

	}
}