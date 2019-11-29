package com.apkupdater.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.apkupdater.R
import com.apkupdater.model.aptoide.aptoideFilters
import com.apkupdater.repository.SelfUpdateRepository
import com.apkupdater.repository.UpdatesRepository
import com.apkupdater.repository.aptoide.AptoideUtils
import com.apkupdater.util.AlarmUtil
import com.apkupdater.util.AppPreferences
import com.apkupdater.util.getAccentColor
import com.apkupdater.util.ifNotEmpty
import com.apkupdater.util.ioScope
import com.apkupdater.util.observe
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import com.google.android.material.snackbar.Snackbar
import com.kryptoprefs.invoke
import com.startapp.android.publish.adsCommon.StartAppAd
import com.startapp.android.publish.adsCommon.StartAppSDK
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.nav_view
import kotlinx.android.synthetic.main.activity_main.swipe_layout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

	private val viewModel: MainViewModel by viewModel()
	private val updatesViewModel: UpdatesViewModel by viewModel()
	private val searchViewModel: SearchViewModel by viewModel()
	private val updatesRepository: UpdatesRepository by inject()
	private val prefs: AppPreferences by inject()
	private val alarmUtil: AlarmUtil by inject()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Ads
		StartAppSDK.init(this, "210024990", false)
		StartAppSDK.setUserConsent (this, "pas", System.currentTimeMillis(), false)
		StartAppAd.disableSplash()

		// Theme and layout
		setTheme()
		setContentView(R.layout.activity_main)

		// Navigation
		val controller = findNavController(R.id.nav_host_fragment)
		nav_view.setupWithNavController(controller)

		// Badges
		viewModel.appsBadge.observe(this) { addBadge(R.id.navigation_apps, it) }
		viewModel.updatesBadge.observe(this) { addBadge(R.id.navigation_updates, it) }
		viewModel.searchBadge.observe(this) { addBadge(R.id.navigation_search, it) }

		// Swipe to refresh
		swipe_layout.setColorSchemeColors(getAccentColor(), getAccentColor(), getAccentColor())
		swipe_layout.setOnRefreshListener { checkForUpdates() }
		viewModel.loading.observe(this) { swipe_layout.isRefreshing = it }
		checkForUpdates()

		// Schedule alarm
		alarmUtil.setupAlarm(applicationContext)

		// Snackbar
		viewModel.snackbar.observe(this) { snackBar(it) }

		// Notification
		if (intent.action == getString(R.string.notification_update_action)){
			prefs.updates().ifNotEmpty {
				updatesViewModel.items.postValue(it)
				viewModel.updatesBadge.postValue(it.size)
			}
			controller.navigate(R.id.navigation_updates)
		}

		// Aptoide filter
		aptoideFilters = AptoideUtils.getFilters(this)

		// SelfUpdate
		checkForSelfUpdate()
	}

	private fun checkForSelfUpdate() = ioScope.launch {
		SelfUpdateRepository().checkForUpdatesAsync(this@MainActivity).await()
			.onFailure { viewModel.snackbar.postValue(it.message) }
	}

	private fun checkForUpdates() = ioScope.launch {
		viewModel.loading.postValue(true)

		updatesRepository.getUpdatesAsync().await()
			.onFailure { viewModel.snackbar.postValue(it.message) }
			.onSuccess {
				updatesViewModel.items.postValue(it)
				viewModel.updatesBadge.postValue(it.size)
			}

		viewModel.loading.postValue(false)
	}

	private fun addBadge(id: Int, num: Int) = runCatching {
		nav_view.removeBadge(id)	// Remove badge, otherwise weird things happen when switching themes
		if (num > 0) nav_view.getOrCreateBadge(id).number = num
		nav_view.getBadge(id)?.verticalOffset = resources.getDimensionPixelSize(R.dimen.badge_offset)
	}.onFailure { e ->
		viewModel.snackbar.postValue(e.message)
		Log.e("addBadge", e.message, e)
	}.getOrNull()

	private fun setTheme() {
		when (prefs.settings.theme) {
			"0" -> setTheme(R.style.AppThemeDarkBlue)
			"1" -> setTheme(R.style.AppThemeLightBlue)
			"2" -> setTheme(R.style.AppThemeDarkOrange)
			"3" -> setTheme(R.style.AppThemeLightOrange)
			else -> setTheme(R.style.AppThemeDarkBlue)
		}
	}

	private fun snackBar(text: String) =
		Snackbar.make(container, text, Snackbar.LENGTH_LONG).apply { setAction(getString(R.string.action_close)) { dismiss() } }.show()

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

		if (resultCode == Activity.RESULT_OK) {
			searchViewModel.remove(requestCode)
			updatesViewModel.remove(requestCode)
			viewModel.snackbar.postValue("App installed.")
		} else {
			updatesViewModel.setLoading(requestCode, false)
			searchViewModel.setLoading(requestCode, false)
			val reason = if (data == null) "Cancelled" else data.extras?.get(data.extras?.keySet()?.first())
			viewModel.snackbar.postValue("Install failed: $reason")
		}

		super.onActivityResult(requestCode, resultCode, data)
	}

}
