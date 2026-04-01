package com.durcheins.apkupdater.util

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream


class Downloader(
    private val client: OkHttpClient,
    private val auroraClient: OkHttpClient,
    private val dir: File
) {

    fun download(url: String): File {
        val file = File(dir, randomUUID())
        client.newCall(downloadRequest(url)).execute().use {
            if (it.isSuccessful) {
                it.body.byteStream().copyTo(file.outputStream())
            }
        }
        return file
    }

    fun downloadStream(url: String): InputStream? = runCatching {
        val c = if (url.contains("aurora")) auroraClient else client
        val response = c.newCall(downloadRequest(url)).execute()
        if (response.isSuccessful) {
            return response.body.byteStream()
        } else {
            response.close()
            Log.e("Downloader", "Download failed with error code: ${response.code}")
        }
        return null
    }.getOrElse {
        Log.e("Downloader", "Error downloading", it)
        null
    }

    private fun downloadRequest(url: String) = Request.Builder().url(url).build()

}
