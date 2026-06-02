package com.apkupdater.util

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.InputStream


class Downloader(
    private val client: OkHttpClient,
    private val apkPureClient: OkHttpClient,
    private val auroraClient: OkHttpClient,
    private val dir: File,
) {

    fun download(url: String): File {
        val file = File(dir, randomUUID())
        client.newCall(downloadRequest(url, emptyMap())).execute().use {
            if (it.isSuccessful) {
                it.body.byteStream().copyTo(file.outputStream())
            }
        }
        return file
    }

    fun downloadResponse(url: String, headers: Map<String, String> = emptyMap()): Response? = runCatching {
        val c = when {
            url.contains("apkpure") -> apkPureClient
            url.contains("aurora") || url.contains("google") || url.contains("android.clients") -> auroraClient
            else -> client
        }
        val request = downloadRequest(url, headers)
        val response = c.newCall(request).execute()
        if (response.isSuccessful) {
            return response
        } else {
            val errorBody = response.body.string()
            response.close()
            Log.e("Downloader", "Download failed with error code: ${response.code}. Response: $errorBody")
        }
        return null
    }.getOrElse {
        Log.e("Downloader", "Error downloading", it)
        null
    }

    fun downloadStream(url: String, headers: Map<String, String> = emptyMap()): InputStream? = 
        downloadResponse(url, headers)?.body?.byteStream()

    private fun downloadRequest(url: String, headers: Map<String, String>) = Request.Builder()
        .url(url)
        .apply {
            if (headers.isEmpty()) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
            } else {
                headers.forEach { (k, v) -> header(k, v) }
            }
        }
        .build()

}
