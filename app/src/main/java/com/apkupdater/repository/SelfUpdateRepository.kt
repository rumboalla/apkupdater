package com.apkupdater.repository

import android.app.Activity
import android.app.AlertDialog
import com.apkupdater.R
import com.apkupdater.model.selfupdate.SelfUpdateResponse
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.app.InstallUtil
import com.apkupdater.util.catchingAsync
import com.apkupdater.util.ioScope
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.kryptoprefs.invoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SelfUpdateRepository: KoinComponent {

	private val installer: InstallUtil by inject()
	private val prefs: AppPrefs by inject()

	private val url = "http://rumboalla.github.io/apkupdater/version.json"
	private val interval = 60 * 60 * 1000

	fun checkForUpdatesAsync(activity: Activity) = ioScope.catchingAsync {
		if (System.currentTimeMillis() - prefs.selfUpdateCheck() > interval) {
			val r = Fuel.get(url).responseString().third.get()
			val o = Gson().fromJson<SelfUpdateResponse>(r, SelfUpdateResponse::class.java)
			prefs.selfUpdateCheck(System.currentTimeMillis())
			if (o.version > activity.packageManager.getPackageInfo(activity.packageName, 0).versionCode) {
				if (withContext(Dispatchers.Main) { showDialog(activity, o) }) {
					installer.install(activity, installer.downloadAsync(activity, o.apk) { _, _ -> }, 0)
				}
			}
		}
	}

	private suspend fun showDialog(activity: Activity, response: SelfUpdateResponse) = suspendCoroutine<Boolean> {
		AlertDialog.Builder(activity, R.style.AlertDialogTheme)
			.setTitle(activity.getString(R.string.selfupdate_found_title))
			.setMessage(response.changelog)
			.setPositiveButton(activity.getString(R.string.action_install)) { _, _: Int -> it.resume(true) }
			.setNegativeButton(activity.getString(android.R.string.cancel)) { _, _ -> it.resume(false) }
			.create()
			.show()
	}

}
