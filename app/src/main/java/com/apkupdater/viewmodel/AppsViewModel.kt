package com.apkupdater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apkupdater.model.ui.AppInstalled

class AppsViewModel : ViewModel() {

	val items = MutableLiveData<List<AppInstalled>>()

}