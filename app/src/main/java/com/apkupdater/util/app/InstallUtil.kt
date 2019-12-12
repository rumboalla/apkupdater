package com.apkupdater.util.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.github.kittinunf.fuel.core.ProgressCallback
import eu.chainfire.libsuperuser.Shell
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InstallUtil: KoinComponent {

	private val prefs: AppPrefs by inject()

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

	@Suppress("DEPRECATION")
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

	private fun rootInstall(file: File): Boolean {
		return Shell.Pool.SU.run("pm install -r ${file.absolutePath}") == 0
	}

	fun install(activity: Activity, file: File, id: Int): Boolean {
		return if (prefs.settings.rootInstall) {
			rootInstall(file)
		} else {
			activity.startActivityForResult(getInstallIntent(activity, file), id)
			false
		}
	}

	suspend fun downloadAsync(context: Context, url: String, file: File = getFile(context), progress: ProgressCallback = { _, _ -> }) = suspendCoroutine<File> {
		clearOldFiles(context)
		get(url, file)
		it.resume(file)
	}

	fun get(url: String, file: File): File {
		val response = OkHttpClient.Builder().followRedirects(true).build().newCall(Request.Builder().url(url).build()).execute()
		if (!response.isSuccessful) throw IOException("Response not successful: ${response.code}")
		file.sink().buffer().apply { writeAll(response.body!!.source()) }.close()
		return file
	}

}
