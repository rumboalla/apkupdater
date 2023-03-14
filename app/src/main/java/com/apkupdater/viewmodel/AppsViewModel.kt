package com.apkupdater.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.apkupdater.model.ui.AppsItem

class AppsViewModel : ViewModel() {

	val items = MutableLiveData<List<AppsItem>>()

}