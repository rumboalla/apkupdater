package com.apkupdater.util.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

	private val pending = AtomicBoolean(false)

	override fun observe(owner: LifecycleOwner, observer: Observer<in T>) =
		super.observe(owner, Observer { if (pending.compareAndSet(true, false)) observer.onChanged(it) })

	override fun setValue(t: T?) {
		pending.set(true)
		super.setValue(t)
	}

}