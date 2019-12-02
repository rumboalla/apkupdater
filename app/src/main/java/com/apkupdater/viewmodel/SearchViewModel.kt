package com.apkupdater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apkupdater.model.ui.AppSearch
import com.apkupdater.util.livedata.SingleLiveEvent

class SearchViewModel : ViewModel() {

	val items = MutableLiveData<List<AppSearch>>()
	val search = SingleLiveEvent<String>()

	fun setLoading(id: Int, loading: Boolean) {
		val list = items.value?.toMutableList()
		list?.find { it.id == id }?.let {
			list[list.indexOf(it)] = list[list.indexOf(it)].copy(loading = loading)
			items.postValue(list)
		}
	}

	fun remove(id: Int) {
		val list = items.value?.toMutableList()
		list?.find { it.id == id }?.let {
			list.remove(it)
			items.postValue(list)
		}
	}

}