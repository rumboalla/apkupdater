package com.apkupdater.util

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.flow.combine

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

inline fun <reified T> List<Flow<T>>.combine(crossinline block: suspend (Array<T>) -> Unit) =
	combine(this) { block(it) }