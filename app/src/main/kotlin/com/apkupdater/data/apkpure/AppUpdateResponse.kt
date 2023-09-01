package com.apkupdater.data.apkpure

import android.net.Uri
import com.apkupdater.data.ui.ApkPureSource
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate


data class AppUpdateResponse(
    val package_name: String,
    val version_code: Long,
    val version_name: String,
    val sign: List<String>,
    val whatsnew: String,
    val description_short: String,
    val label: String,
    val asset: AppUpdateResponseAsset,
    val icon: AppUpdateResponseIcon
)

fun AppUpdateResponse.toAppUpdate(
    app: AppInstalled?
) = AppUpdate(
    label,
    package_name,
    version_name,
    app?.version.orEmpty(),
    version_code,
    app?.versionCode ?: 0L,
    ApkPureSource,
    if (app == null) Uri.parse(icon.thumbnail.url) else Uri.EMPTY,
    asset.url.replace("http://", "https://"),
    if (app == null) description_short else whatsnew
)
