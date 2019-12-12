package com.apkupdater.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.apkupdater.R
import com.apkupdater.model.ui.AppInstalled
import com.apkupdater.repository.AppsRepository
import com.apkupdater.util.adapter.BindAdapter
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.observe
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.bumptech.glide.Glide
import com.kryptoprefs.invoke
import kotlinx.android.synthetic.main.fragment_apps.recycler_view
import kotlinx.android.synthetic.main.view_apps.view.action_one
import kotlinx.android.synthetic.main.view_apps.view.container
import kotlinx.android.synthetic.main.view_apps.view.icon
import kotlinx.android.synthetic.main.view_apps.view.name
import kotlinx.android.synthetic.main.view_apps.view.packageName
import kotlinx.android.synthetic.main.view_apps.view.version
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class AppsFragment : Fragment() {

	private val repository: AppsRepository by inject()
	private val prefs: AppPrefs by inject()
	private val appsViewModel: AppsViewModel by viewModel()
	private val mainViewModel: MainViewModel by sharedViewModel()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		inflater.inflate(R.layout.fragment_apps, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		recycler_view.layoutManager = LinearLayoutManager(context)
		val adapter = BindAdapter(R.layout.view_apps, onBind)
		recycler_view.adapter = adapter
		appsViewModel.items.observe(this) {
			adapter.items = it
			mainViewModel.appsBadge.postValue(it.size)
		}
		updateApps()
	}

	private val onBind = { view: View, app: AppInstalled ->
		view.name.text = app.name
		view.packageName.text = app.packageName
		view.version.text = view.context.getString(R.string.version_version_code, app.version, app.versionCode)
		Glide.with(view).load(app.iconUri).into(view.icon)
		view.action_one.text = getString(if (app.ignored) R.string.action_unignore else R.string.action_ignore)
		view.action_one.setOnClickListener { onIgnoreClick(app) }
		view.container.alpha = if (app.ignored) 0.4f else 1.0f
	}

	private val onIgnoreClick = { app: AppInstalled ->
		val ignoredApps = prefs.ignoredApps().toMutableList()
		if (ignoredApps.contains(app.packageName)) ignoredApps.remove(app.packageName) else ignoredApps.add(app.packageName)
		prefs.ignoredApps(ignoredApps)
		updateApps()
	}

	private fun updateApps() = lifecycle.coroutineScope.launch {
		appsViewModel.items.postValue(repository.getApps())
	}

}