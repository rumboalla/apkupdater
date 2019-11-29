package com.apkupdater.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ProgressCallback
import kotlinx.coroutines.async
import java.io.File
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InstallUtil {

	private val fileProvider = "com.apkupdater.fileprovider"
	private val downloadDir = "downloads"
	private val mime = "application/vnd.android.package-archive"

	private fun clearOldFiles(context: Context) {
		val dir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) File(context.cacheDir, downloadDir) else context.externalCacheDir
		dir?.walkTopDown()?.filter { System.currentTimeMillis() - it.lastModified() > 1_200_000 }?.forEach { it.delete() }
	}

	private fun getFile(context: Context): File {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			val dir = File(context.cacheDir, downloadDir)
			dir.mkdirs()
			File(dir, UUID.randomUUID().toString())
		} else {
			File(context.externalCacheDir, UUID.randomUUID().toString())
		}
	}

	private fun getInstallIntent(activity: Activity, file: File): Intent {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
				setDataAndType(FileProvider.getUriForFile(activity, fileProvider, file), mime)
				flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
				putExtra(Intent.EXTRA_RETURN_RESULT, true)
			}
		} else {
			Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
				setDataAndType(file.toUri(), mime)
				putExtra(Intent.EXTRA_RETURN_RESULT, true)
			}
		}
	}

	fun install(activity: Activity, file: File, id: Int) = activity.startActivityForResult(getInstallIntent(activity, file), id)

	fun downloadAsync(activity: Activity, url: String, progress: ProgressCallback) = ioScope.async {
		clearOldFiles(activity)
		val file = getFile(activity)
		Fuel.download(url).fileDestination { _, _ -> file }.progress(progress).response().third.fold(
			success = { Result.success(file) },
			failure = { Result.failure(it) }
		)
	}

	suspend fun downloadAsync2(activity: Activity, url: String, progress: ProgressCallback) = suspendCoroutine<File> {
		val file = getFile(activity)
		clearOldFiles(activity)
		Fuel.download(url).fileDestination { _, _ -> file }.progress(progress).response().third.get()
		it.resume(file)
	}

}
