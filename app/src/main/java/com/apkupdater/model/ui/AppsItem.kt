package com.apkupdater.model.ui

import android.net.Uri
import androidx.annotation.StringRes
import com.apkupdater.R

data class AppsItem (
    val name: String,
    val packageName: String,
    val version: String,
    val iconUri: Uri,
    val alpha: Float,
    @StringRes val action: Int
)

inline val AppInstalled.model: AppsItem get() = AppsItem(
    name,
    packageName,
    "$version ($versionCode)",
    iconUri,
    if (ignored) 0.4f else 1.0f,
    if (ignored) R.string.action_unignore else R.string.action_ignore
)