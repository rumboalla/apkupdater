package com.apkupdater.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import com.apkupdater.model.ui.Action
import com.apkupdater.model.ui.AppsItem
import com.apkupdater.model.ui.model
import com.apkupdater.repository.AppsRepository
import com.apkupdater.ui.ActionRow
import com.apkupdater.ui.AppInfo
import com.apkupdater.ui.CustomCard
import com.apkupdater.util.app.AppPrefs
import com.apkupdater.util.observe
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.google.accompanist.themeadapter.material.MdcTheme
import com.kryptoprefs.invoke
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
		ComposeView(requireContext()).apply { setContent { AppFragment() } }

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		appsViewModel.items.observe<List<AppsItem>>(this) {
			mainViewModel.appsBadge.postValue(it.size)
		}
		updateApps()
	}

	private val onIgnoreClick = { app: AppsItem ->
		val ignoredApps = prefs.ignoredApps().toMutableList()
		if (ignoredApps.contains(app.packageName)) ignoredApps.remove(app.packageName) else ignoredApps.add(app.packageName)
		prefs.ignoredApps(ignoredApps)
		updateApps()
	}

	private fun updateApps() = lifecycle.coroutineScope.launch {
		appsViewModel.items.postValue(repository.getApps().map { it.model })
	}
	@Preview
	@Composable
	fun AppFragment() = MdcTheme {
		val state = appsViewModel.items.observeAsState()

		LazyColumn {
			items(state.value.orEmpty()) { AppCard(it) }
			item { Spacer(modifier = Modifier.size(60.dp)) }
		}
	}

	@Composable
	fun AppCard(app: AppsItem) = CustomCard(app.alpha) {
		Column(modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp)) {
			AppInfo(app)
			ActionRow(actionOne = Action(stringResource(app.action)) { onIgnoreClick(app) })
		}
	}

}