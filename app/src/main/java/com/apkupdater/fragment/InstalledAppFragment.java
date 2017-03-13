package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.widget.ListView;

import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_installed_apps)
public class InstalledAppFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	ListView mListView;

	@Bean
	InstalledAppAdapter mAdapter;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	MyBus mBus;

	List<InstalledApp> mItems;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ItemLongClick(R.id.list_view)
	void onUpdateClicked(
		InstalledApp app
	) {
		// Get the ignore list from the options
		UpdaterOptions options = new UpdaterOptions(getContext());
		List<String> ignore_list = options.getIgnoreList();

		// If it's on the ignore remove, otherwise add it
		if (ignore_list.contains(app.getPname())) {
			ignore_list.remove(app.getPname());
		} else {
			ignore_list.add(app.getPname());
		}

		// Update the list
		options.setIgnoreList(ignore_list);

		// Sort it
		sort(mItems);

		setListAdapter(mItems);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mBus.register(this);
		ViewCompat.setNestedScrollingEnabled(mListView, true);
		updateInstalledApps(new UpdateInstalledAppsEvent());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy() {
		mBus.unregister(this);
		super.onDestroy();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void updateInstalledApps(
		UpdateInstalledAppsEvent ev
	) {
		mInstalledAppUtil.getInstalledAppsAsync(getContext(), new GenericCallback<List<InstalledApp>>() {
			@Override
			public void onResult(List<InstalledApp> items) {
				mItems = items;
				setListAdapter(items);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> sort(
		List<InstalledApp> items
	) {
		// Lists to hold both types of apps
		List<InstalledApp> normal = new ArrayList<>();
		List<InstalledApp> ignored = new ArrayList<>();

		// Get the ignore list
		UpdaterOptions options = new UpdaterOptions(getContext());
		List<String> ignore_list = options.getIgnoreList();

		// Iterate and buld the temp lists
		for (InstalledApp i : items) {
			if (ignore_list.contains(i.getPname())) {
				ignored.add(i);
			} else {
				normal.add(i);
			}
		}

		// Build comparator
		Comparator<InstalledApp> comparator = new Comparator<InstalledApp>() {
			@Override
			public int compare(InstalledApp o1, InstalledApp o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};

		// Sort them
		Collections.sort(normal, comparator);
		Collections.sort(ignored, comparator);

		// Build final
		List<InstalledApp> ordered = new ArrayList<>();
		for (InstalledApp i : normal) {
			ordered.add(i);
		}

		for (InstalledApp i : ignored) {
			ordered.add(i);
		}

		return ordered;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	protected void setListAdapter(
		List<InstalledApp> items
	) {
		if (mAdapter == null || mListView == null || mBus == null) {
			return;
		}

		mAdapter.clear();
		mListView.setAdapter(mAdapter);

		// Animation
		if (Build.VERSION.SDK_INT >= 21) {
			TransitionManager.beginDelayedTransition(mListView, new Slide());
		} else {
			android.support.transition.TransitionManager.beginDelayedTransition(mListView);
		}

		for (InstalledApp i : sort(items)) { // addAll needs API level 11+
			mAdapter.add(i);
		}


		mBus.post(new InstalledAppTitleChange(getString(R.string.tab_installed) + " (" + items.size() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
