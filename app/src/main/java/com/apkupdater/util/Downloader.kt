package com.apkupdater.util

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.UUID


class Downloader(context: Context) {

    private val client = OkHttpClient.Builder().followRedirects(true).build()
    private val dir =  File(context.cacheDir, "downloads").apply { mkdirs() }

    fun download(url: String): File {
        val request = Request.Builder().url(url).build()
        val file = File(dir, UUID.randomUUID().toString())
        client.newCall(request).execute().use {
            if (it.isSuccessful) {
                it.body?.byteStream()?.copyTo(file.outputStream())
            }
        }
        return file
    }

}
