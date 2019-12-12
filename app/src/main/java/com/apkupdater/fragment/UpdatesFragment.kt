package com.apkupdater.fragment

import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.apkupdater.R
import com.apkupdater.model.ui.AppUpdate
import com.apkupdater.repository.googleplay.GooglePlayRepository
import com.apkupdater.util.adapter.BindAdapter
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.app.InstallUtil
import com.apkupdater.util.getAccentColor
import com.apkupdater.util.iconUri
import com.apkupdater.util.ioScope
import com.apkupdater.util.launchUrl
import com.apkupdater.util.observe
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_apps.recycler_view
import kotlinx.android.synthetic.main.view_apps.view.action_one
import kotlinx.android.synthetic.main.view_apps.view.icon
import kotlinx.android.synthetic.main.view_apps.view.name
import kotlinx.android.synthetic.main.view_apps.view.packageName
import kotlinx.android.synthetic.main.view_apps.view.progress
import kotlinx.android.synthetic.main.view_apps.view.source
import kotlinx.android.synthetic.main.view_apps.view.version
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class UpdatesFragment : Fragment() {

	private val updatesViewModel: UpdatesViewModel by sharedViewModel()
	private val mainViewModel: MainViewModel by sharedViewModel()
	private val installer: InstallUtil by inject()
	private val prefs: AppPrefs by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		inflater.inflate(R.layout.fragment_updates, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		recycler_view.layoutManager = LinearLayoutManager(context)
		val adapter = BindAdapter(R.layout.view_apps, onBind)
		recycler_view.adapter = adapter
		(recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
		updatesViewModel.items.observe(this) {
			it?.let {
				adapter.items = it
				mainViewModel.updatesBadge.postValue(it.size)
			}
		}
	}

	private val onBind = { view: View, app: AppUpdate ->
        runCatching {
            view.name.text = app.name
            view.packageName.text = app.packageName
            view.version.text = getString(R.string.update_version_version_code, app.oldVersion, app.oldCode, app.version, app.versionCode)
            view.action_one.text = getString(R.string.action_install)
            if (app.loading) {
                view.progress.visibility = View.VISIBLE
                view.action_one.visibility = View.INVISIBLE
            } else {
                view.progress.visibility = View.INVISIBLE
                view.action_one.visibility = View.VISIBLE
                view.action_one.text = getString(R.string.action_install)
                view.action_one.setOnClickListener { if (app.url.endsWith("apk") || app.url == "play") downloadAndInstall(app) else launchUrl(app.url) }
            }
            view.source.setColorFilter(view.context.getAccentColor(), PorterDuff.Mode.MULTIPLY)
			Glide.with(view).load(app.source).into(view.source)
            Glide.with(view).load(iconUri(app.packageName, view.context.packageManager.getApplicationInfo(app.packageName, 0).icon)).into(view.icon)
        }.onFailure { Log.e("UpdatesFragment", "onBind", it) }.let { Unit }
	}

	private fun downloadAndInstall(app: AppUpdate) = ioScope.launch {
		runCatching {
			updatesViewModel.setLoading(app.id, true)
			val url = if (app.url == "play") googlePlayRepository.getDownloadUrl(app.packageName, app.versionCode, app.oldCode) else app.url
			val file = installer.downloadAsync(requireActivity(), url) { _, _ -> updatesViewModel.setLoading(app.id, true) }
			if(installer.install(requireActivity(), file, app.id)) {
				updatesViewModel.setLoading(app.id, false)
				updatesViewModel.remove(app.id)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_success))
			} else if (prefs.settings.rootInstall) {
				updatesViewModel.setLoading(app.id, false)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_failure))
			}
		}.onFailure {
			updatesViewModel.setLoading(app.id, false)
			mainViewModel.snackbar.postValue(it.message ?: "downloadAndInstall error.")
		}
	}

}