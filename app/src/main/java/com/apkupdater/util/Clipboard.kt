package com.apkupdater.util

import android.content.ClipData
import android.content.Context


class Clipboard(private val context: Context) {

    fun copy(text: String, label: String = "Copied Text") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

}
