package com.apkupdater.util

import android.content.Context

class Stringer(val context: Context) {

    fun get(id: Int) = context.getString(id)
    fun get(id: Int, vararg params: Any?) = context.getString(id, *params)

}
