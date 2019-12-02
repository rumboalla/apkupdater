package com.apkupdater.model.ui

import com.apkupdater.util.adapter.Id

data class AppInstalled(
	override val id: Int,
	val name: String,
	val packageName: String,
	val version: String,
	val versionCode: Int,
	val ignored: Boolean = false
): Id