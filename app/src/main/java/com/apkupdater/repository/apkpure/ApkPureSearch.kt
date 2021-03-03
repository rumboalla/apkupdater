package com.apkupdater.repository.apkpure

import com.apkupdater.R
import com.apkupdater.model.ui.AppSearch
import com.apkupdater.util.ioScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import org.koin.core.KoinComponent

class ApkPureSearch: KoinComponent {

	private val baseUrl = "https://apkpure.com"
	private val searchQuery = "/search?q="
	private val source = R.drawable.apkpure_logo

	private fun search(text: String): List<AppSearch> {
		val doc = Jsoup.connect("$baseUrl$searchQuery$text").get()
		val rowsWithApps = doc.select("dl")
		return rowsWithApps.map {
			AppSearch(
				it.select("dd > p").first().text(),
				"$baseUrl${it.select("dd > p > a").attr("href")}",
				it.select("dt > a > img").attr("src"),
				it.select("dd > p")[1].select("a").text(),
				source
			)
		}
	}

	fun searchAsync(text: String) = ioScope.async { runCatching { search(text) } }

}
