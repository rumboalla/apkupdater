package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.support.v4.app.Fragment;
import android.widget.ListView;

import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemLongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

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

		// Notify of changes
		mAdapter.notifyDataSetChanged();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mInstalledAppUtil.getInstalledAppsAsync(getContext(), new GenericCallback<List<InstalledApp>>() {
			@Override
			public void onResult(List<InstalledApp> items) {
				setListAdapter(items);
			}
		});
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

		for (InstalledApp i : items) { // addAll needs API level 11+
			mAdapter.add(i);
		}

		mListView.setAdapter(mAdapter);
		mBus.post(new InstalledAppTitleChange(getString(R.string.tab_installed) + " (" + items.size() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
