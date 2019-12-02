package com.apkupdater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apkupdater.util.livedata.SingleLiveEvent

class MainViewModel : ViewModel() {

	val appsBadge = MutableLiveData<Int>()
	val updatesBadge = MutableLiveData<Int>()
	val searchBadge = MutableLiveData<Int>()
	val snackbar = SingleLiveEvent<String>()
	val loading = SingleLiveEvent<Boolean>()

}