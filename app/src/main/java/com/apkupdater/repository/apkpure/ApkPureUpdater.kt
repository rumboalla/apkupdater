package com.apkupdater.repository.apkpure

import android.os.Build
import com.apkupdater.R
import com.apkupdater.model.apkpure.VerInfo
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.ifNotEmpty
import com.apkupdater.util.ioScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.koin.core.KoinComponent

class ApkPureUpdater(private val prefs: AppPrefs) : KoinComponent {

	private val baseUrl = "https://apkpure.com"
	private val searchQuery = "/search?q="
	private val versionsUrl = "/versions"
	private val source = R.drawable.apkpure_logo
	private val excludeArch get() = prefs.settings.excludeArch
	private val excludeMinApi get() = prefs.settings.excludeMinApi
	private val arch get() = Build.CPU_ABI
	private val api get() = Build.VERSION.SDK_INT

	private fun getApkPackageLink(name: String): String {
		val doc = Jsoup.connect("$baseUrl$searchQuery$name").get()
		doc.select("dl > dt > a[href*=$name]")?.let { return it.attr("href") }
		return ""
	}

	private fun getAbsoluteVersionsLink(packageLink: String): String {
		return baseUrl + packageLink + versionsUrl
	}

	private fun getVariantsPage(element: Element): Document {
		return Jsoup.connect(baseUrl + element.select("div.ver > ul.ver-wrap > li > a").attr("href")).get()
	}

	private fun hasVariants(element: Element): Boolean {
		return element.select("div.ver > ul.ver-wrap > li > a").attr("title").contains("build variant")
	}

	private fun resolveVersionsPage(packageLink: String): Pair<Boolean, Element> {
		val element = Jsoup.connect(getAbsoluteVersionsLink(packageLink)).ignoreHttpErrors(true).get()
		return Pair(!element.getElementsByTag("title").text().contains("404"), element)
	}

	private fun crawlVersionsPage(element: Element, verInfos: MutableList<VerInfo>, app: AppInstalled) {
		if (hasVariants(element)) {
			getVariantsPage(element).select("div.ver-info").forEach { variant ->
				verInfos.add(VerInfo(variant, app.packageName))
			}
		} else {
			val verInfo = VerInfo(element.select("div.ver > ul.ver-wrap > li > div.ver-info").first(), app.packageName)
			verInfos.add(verInfo)
		}
	}

	private fun crawlUpdates(verInfos: MutableList<VerInfo>, app: AppInstalled) {
		getApkPackageLink(app.packageName).ifNotEmpty { packageLink ->
			val (gotVersionsPage, element) = resolveVersionsPage(packageLink)
			if (gotVersionsPage) {
				crawlVersionsPage(element, verInfos, app)
			}
		}
	}

	fun updateAsync(apps: Sequence<AppInstalled>) = ioScope.async {
		val updates = mutableListOf<AppUpdate>()
		val verInfos = mutableListOf<VerInfo>()
		val jobs = mutableListOf<Job>()
		val mutex = Mutex()

		apps.forEach { app ->
			launch {
				crawlUpdates(verInfos, app)
			}.let { mutex.withLock { jobs.add(it) } }
		}
		jobs.forEach { it.join() }

		verInfos.filter {
			!excludeArch || it.architectures.contains(arch)
		}.filter {
			!excludeMinApi || it.minApiLevel <= api
		}.forEach { verInfo ->
			apps.find { app -> app.packageName == verInfo.packageName }?.let {
				app -> updates.add(AppUpdate.from(app, verInfo))
			}
		}

		Result.success(updates)
	}

	private fun AppUpdate.Companion.from(app: AppInstalled, verInfo: VerInfo) =
			AppUpdate(
					app.name,
					app.packageName,
					verInfo.versionName,
					verInfo.versionCode,
					app.version,
					app.versionCode,
					"$baseUrl${verInfo.downloadLink}",
					source
			)
}
