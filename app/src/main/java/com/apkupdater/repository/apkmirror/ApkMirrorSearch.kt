package com.apkupdater.repository.apkmirror

import com.apkupdater.R
import com.apkupdater.model.ui.AppSearch
import com.apkupdater.util.ioScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import org.koin.core.KoinComponent

class ApkMirrorSearch: KoinComponent {

	private val baseUrl = "https://www.apkmirror.com"
	private val searchQuery = "/?post_type=app_release&searchtype=app&s="
	private val source = R.drawable.apkmirror_logo

	private fun search(text: String): List<AppSearch> {
		val doc = Jsoup.connect("$baseUrl$searchQuery$text").get()
		val row = doc.select("div.appRow")
		val a = row.select("a.byDeveloper")
		val h5 = row.select("h5.appRowTitle").take(a.size)
		val img = row.select("img")
		return (0 until a.size).map {
			AppSearch(
				h5[it].attr("title"),
				"$baseUrl${h5[it].selectFirst("a").attr("href")}",
				"$baseUrl${img[it].attr("src")}".replace("=32", "=64"),
				a[it].text(),
				source
			)
		}
	}

	fun searchAsync(text: String) = ioScope.async { runCatching { search(text) } }

}