package com.apkupdater.util

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import java.util.Calendar
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

// A clickable modifier that will disable the default ripple
fun Modifier.clickableNoRipple(onClick: () -> Unit) =
	clickable(MutableInteractionSource(), null, onClick = onClick)

// Launches a coroutine and executes it inside the mutex
fun CoroutineScope.launchWithMutex(
	mutex: Mutex,
	context: CoroutineContext = EmptyCoroutineContext,
	block: suspend CoroutineScope.() -> Unit
) = launch(context) {
	mutex.withLock {
		block()
	}
}

fun <T> MutableList<T>.update(newItems: List<T>) {
	clear()
	addAll(newItems)
}

inline fun <T> List<T>.ifNotEmpty(block: (List<T>) -> Unit) {
	if (isNotEmpty()) block(this)
}

fun Boolean?.orFalse() = this ?: false

fun PackageInfo.name(context: Context) = applicationInfo.loadLabel(context.packageManager).toString()

fun Context.getAppIcon(packageName: String) = packageManager.getApplicationIcon(packageName)

fun Context.getAppName(packageName: String): String = packageManager.getPackageInfo(packageName, 0).name(this)

inline fun <reified T> List<Flow<T>>.combine(crossinline block: suspend (Array<T>) -> Unit) =
	combine(this) { block(it) }

fun ByteArray.toSha1(): String = MessageDigest
	.getInstance("SHA-1")
	.digest(this)
	.joinToString(separator = "", transform = { "%02x".format(it) })

@Suppress("DEPRECATION")
fun PackageInfo.getSignatureHash(): String = runCatching {
	if (Build.VERSION.SDK_INT >= 28) {
		signingInfo.apkContentsSigners[0].toByteArray().toSha1()
	} else {
		signatures[0].toByteArray().toSha1()
	}
}.getOrDefault("")

fun millisUntilHour(hour: Int): Long {
	val calendar = Calendar.getInstance()
	if (calendar.get(Calendar.HOUR_OF_DAY) >= hour) calendar.add(Calendar.HOUR, 24)
	calendar.set(Calendar.HOUR_OF_DAY, hour)
	calendar.set(Calendar.MINUTE, 0)
	return calendar.timeInMillis - System.currentTimeMillis()
}
