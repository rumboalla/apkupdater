package com.apkupdater.data.ui

import android.net.Uri

data class AppUpdate(
	val name: String,
	val packageName: String,
	val version: String,
	val oldVersion: String,
	val versionCode: Long,
	val oldVersionCode: Long,
	val source: Source,
	val iconUri: Uri = Uri.EMPTY,
	val link: Link = Link.Empty,
	val whatsNew: String = "",
	val isPersistent: Boolean = false,
	val isInstalling: Boolean = false,
	val total: Long = 0L,
	val progress: Long = 0L,
	val status: String = "",
	val error: String? = null,
	val id: Int = "${source.name}.$packageName.$versionCode.$version".hashCode()
)

fun List<AppUpdate>.indexOf(id: Int) = indexOfFirst { it.id == id }

fun MutableList<AppUpdate>.setIsInstalling(id: Int, b: Boolean): List<AppUpdate> {
	val index = this.indexOf(id)
	if (index != -1) {
		this[index] = this[index].copy(isInstalling = b, error = null)
	}
	return this
}

fun MutableList<AppUpdate>.setStatus(id: Int, status: String): List<AppUpdate> {
	val index = this.indexOf(id)
	if (index != -1) {
		this[index] = this[index].copy(status = status)
	}
	return this
}

fun MutableList<AppUpdate>.setError(id: Int, error: String?): List<AppUpdate> {
	val index = this.indexOf(id)
	if (index != -1) {
		this[index] = this[index].copy(error = error, isInstalling = false)
	}
	return this
}

fun MutableList<AppUpdate>.removeId(id: Int): List<AppUpdate> {
	val index = this.indexOf(id)
	if (index != -1) this.removeAt(index)
	return this
}

fun MutableList<AppUpdate>.setProgress(progress: AppInstallProgress): MutableList<AppUpdate> {
	val index = this.indexOf(progress.id)
	if (index != -1) {
		val current = this[index]
		val newTotal = progress.total ?: current.total
		val newProgress = progress.progress ?: current.progress

		// Ensure progress is monotonic (never decreases) and clamped
		if (newTotal > 0) {
			val clampedProgress = newProgress.coerceIn(current.progress, newTotal)
			this[index] = current.copy(progress = clampedProgress, total = newTotal)
		} else if (newProgress >= current.progress) {
			this[index] = current.copy(progress = newProgress)
		}
	}
	return this
}
