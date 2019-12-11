package com.apkupdater.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

fun PackageInfo.name(context: Context) = applicationInfo.loadLabel(context.packageManager).toString()

fun <T> LiveData<T>.observe(owner: LifecycleOwner, block: (T) -> Unit) = observe(owner, Observer { block(it) })

fun Fragment.launchUrl(url: String) = runCatching { startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) }) }
    .onFailure { Log.e("Extensions", "launchUrl", it) }
    .getOrNull()

val ioScope = CoroutineScope(Dispatchers.IO)

val uiScope = CoroutineScope(Dispatchers.Main)

fun <T> CoroutineScope.catchingAsync(block: suspend () -> T): Deferred<Result<T>> = ioScope.async { runCatching { block() } }

fun Context.getAccentColor() = TypedValue().apply { theme.resolveAttribute(resources.getIdentifier("colorAccent", "attr", packageName), this, true) }.data

fun <T: Collection<*>> T.ifNotEmpty(block: (T) -> Unit) = if (isNotEmpty()) block(this) else Unit

fun String.ifNotEmpty(block: (String) -> Unit) = if (isNotEmpty()) block(this) else Unit

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int?.orZero() = this ?: 0

fun iconUri(packageName: String, id: Int): Uri = Uri.parse("android.resource://$packageName/$id")

fun Boolean?.orFalse() = this ?: false