package com.apkupdater.model.ui

import android.net.Uri
import com.apkupdater.util.adapter.Id

data class AppInstalled(
	override val id: Int,
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Int,
	val iconUri: Uri = Uri.EMPTY,
	val ignored: Boolean = false
): Id