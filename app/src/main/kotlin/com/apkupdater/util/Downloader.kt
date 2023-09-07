package com.apkupdater.util

import android.content.Context
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream


class Downloader(
    context: Context,
    cache: Cache
) {

    private val client = OkHttpClient.Builder().followRedirects(true).cache(cache).build()
    private val apkPureClient = OkHttpClient.Builder().followRedirects(true).cache(cache).addNetworkInterceptor {
        it.proceed(it.request().newBuilder().header("user-agent", "APKPure/3.19.39 (Aegon)").build())
    }.build()
    private val dir =  File(context.cacheDir, "downloads").apply { mkdirs() }

    fun download(url: String): File {
        val file = File(dir, randomUUID())
        client.newCall(downloadRequest(url)).execute().use {
            if (it.isSuccessful) {
                it.body?.byteStream()?.copyTo(file.outputStream())
            }
        }
        return file
    }

    fun downloadStream(url: String): InputStream? = runCatching {
        val c = when {
            url.contains("apkpure") -> apkPureClient
            else -> client
        }
        val response = c.newCall(downloadRequest(url)).execute()
        if (response.isSuccessful) {
            response.body?.let {
                return it.byteStream()
            }
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
