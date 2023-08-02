package com.apkupdater.util

import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.InputStream
import java.util.UUID


class Downloader(context: Context) {

    private val client = OkHttpClient.Builder().followRedirects(true).build()
    private val dir =  File(context.cacheDir, "downloads").apply { mkdirs() }

    @Suppress("unused")
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
        val response = client.newCall(downloadRequest(url)).execute()
        if (response.isSuccessful) {
            response.body?.let {
                return it.byteStream()
            }
        }
        return null
    }.getOrElse {
        Log.e("Downloader", "Error downloading", it)
        null
    }

    private fun downloadRequest(url: String) = Request.Builder().url(url).build()

}
