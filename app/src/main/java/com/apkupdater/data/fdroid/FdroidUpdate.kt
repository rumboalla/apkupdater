package com.apkupdater.data.fdroid

import androidx.core.net.toUri
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Source

data class FdroidUpdate(
    val apk: FdroidPackage,
    val app: FdroidApp
)

fun FdroidUpdate.toAppUpdate(current: AppInstalled?, source: Source) = AppUpdate(
    app.name,
    app.packageName,
    apk.versionName,
    current?.version ?: "?",
    apk.versionCode,
    current?.versionCode ?: 0L,
    source,
    if(app.icon.isEmpty())
        "https://f-droid.org/assets/ic_repo_app_default.png".toUri()
    else
        "https://f-droid.org/repo/icons-640/${app.icon}".toUri(),
    "https://f-droid.org/repo/${apk.apkName}",
    if (current != null) app.localized["en-US"]?.whatsNew.orEmpty() else app.localized["en-US"]?.summary.orEmpty()
)
