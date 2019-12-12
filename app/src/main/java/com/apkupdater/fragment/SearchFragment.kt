package com.apkupdater.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.apkupdater.R
import com.apkupdater.model.ui.AppSearch
import com.apkupdater.repository.SearchRepository
import com.apkupdater.repository.googleplay.GooglePlayRepository
import com.apkupdater.util.adapter.BindAdapter
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.app.InstallUtil
import com.apkupdater.util.getAccentColor
import com.apkupdater.util.ifNotEmpty
import com.apkupdater.util.ioScope
import com.apkupdater.util.launchUrl
import com.apkupdater.util.observe
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_apps.recycler_view
import kotlinx.android.synthetic.main.fragment_search.text
import kotlinx.android.synthetic.main.view_apps.view.action_one
import kotlinx.android.synthetic.main.view_apps.view.icon
import kotlinx.android.synthetic.main.view_apps.view.name
import kotlinx.android.synthetic.main.view_apps.view.packageName
import kotlinx.android.synthetic.main.view_apps.view.progress
import kotlinx.android.synthetic.main.view_apps.view.source
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class SearchFragment : Fragment() {

	private val searchViewModel: SearchViewModel by sharedViewModel()
	private val mainViewModel: MainViewModel by sharedViewModel()
	private val searchRepository: SearchRepository by inject()
	private val googlePlayRepository: GooglePlayRepository by inject()
	private val installer: InstallUtil by inject()
	private val prefs: AppPrefs by inject()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
		inflater.inflate(R.layout.fragment_search, container, false)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		// RecyclerView
		recycler_view.layoutManager = LinearLayoutManager(context)
		val adapter = BindAdapter(R.layout.view_apps, onBind)
		recycler_view.adapter = adapter
		(recycler_view.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
		searchViewModel.items.observe(this) {
			it?.let {
				adapter.items = it
				mainViewModel.searchBadge.postValue(it.size)
			}
		}

		// Search
		text.setOnEditorActionListener { text, id, event ->
			if (id == EditorInfo.IME_ACTION_SEARCH) {
				searchViewModel.search.postValue(text.text.toString())
				val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				imm.hideSoftInputFromWindow(text.windowToken, 0)
				true
			} else {
				false
			}
		}

		searchViewModel.search.observe(this) { search(it) }
	}

	private fun search(text: String) = ioScope.launch {
		mainViewModel.loading.postValue(true)
		searchRepository.getSearchResultsAsync(text).await().fold(
			onSuccess = { searchViewModel.items.postValue(it) },
			onFailure = {
				mainViewModel.snackbar.postValue(it.message ?: "search error.")
				Log.e("SearchFragment", "search", it)
			}
		)
	}.invokeOnCompletion { mainViewModel.loading.postValue(false) }

	private val onBind = { view: View, app: AppSearch ->
		app.iconurl.ifNotEmpty { Glide.with(view).load(it).placeholder(ColorDrawable(Color.BLACK)).error(ColorDrawable(Color.RED)).into(view.icon) }
		view.name.text = app.name
		view.packageName.text = app.developer

		if (app.loading) {
			view.progress.visibility = View.VISIBLE
			view.action_one.visibility = View.INVISIBLE
		} else {
			view.progress.visibility = View.INVISIBLE
			view.action_one.visibility = View.VISIBLE
			view.action_one.text = getString(R.string.action_install)
			view.action_one.setOnClickListener { if (app.url.endsWith("apk") || app.url == "play")  downloadAndInstall(app) else launchUrl(app.url) }
		}
		Glide.with(view).load(app.source).into(view.source)
		view.source.setColorFilter(view.context.getAccentColor(), PorterDuff.Mode.MULTIPLY)
	}

	private fun downloadAndInstall(app: AppSearch) = ioScope.launch {
		runCatching {
			searchViewModel.setLoading(app.id, true)
			val url = if (app.url == "play") googlePlayRepository.getDownloadUrl(app.packageName, app.versionCode, 0) else app.url
			val file = installer.downloadAsync(requireActivity(), url) { _, _ -> searchViewModel.setLoading(app.id, true) }
			if(installer.install(requireActivity(), file, app.id)) {
				searchViewModel.setLoading(app.id, false)
				searchViewModel.remove(app.id)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_success))
			} else if (prefs.settings.rootInstall) {
				searchViewModel.setLoading(app.id, false)
				mainViewModel.snackbar.postValue(getString(R.string.app_install_failure))
			}
		}.onFailure {
			searchViewModel.setLoading(app.id, false)
			mainViewModel.snackbar.postValue(it.message ?: "downloadAndInstall failure.")
		}
	}

}