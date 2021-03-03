package com.apkupdater.model.apkpure

import org.jsoup.nodes.Element

class VerInfo(element: Element, packageName: String) {
	val packageName = packageName
	val versionName: String
	val versionCode: Int
	val minApiLevel: Int
	val architectures: List<String>
	val downloadLink: String

	init {
		val versionNameAndCode = element.getElementsByClass("ver-info-top").text().split(" ").takeLast(2)
		versionName = versionNameAndCode[0]
		versionCode = versionNameAndCode[1].removeSurrounding("(", ")").toInt()
		minApiLevel = element.getElementsByTag("p")[2]
				.text().split(" ").last().removeSuffix(")").toInt()
		architectures = element.getElementsByTag("p")[5].text().replace(",", "").split(" ").drop(0)
		downloadLink = element.getElementsByClass(" down").attr("href")
	}
}
