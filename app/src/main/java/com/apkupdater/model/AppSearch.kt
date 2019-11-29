package com.apkupdater.model

import com.apkupdater.util.adapter.Id
import com.apkupdater.util.crc16

data class AppSearch(
	val name: String,
	val url: String = "",
	val iconurl: String = "",
	val developer: String = "",
	val source: Int = 0,
	override val id: Int = crc16(url),
	var loading: Boolean = false
): Id