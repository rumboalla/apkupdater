package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
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
	RecyclerView mRecyclerView;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	MyBus mBus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mBus.register(this);
		ViewCompat.setNestedScrollingEnabled(mRecyclerView, true);
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
				setListAdapter(items);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	protected void setListAdapter(
		List<InstalledApp> items
	) {
		if (mRecyclerView == null || mBus == null) {
			return;
		}

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setAdapter(new InstalledAppAdapter(getContext(), mRecyclerView, items));

		mBus.post(new InstalledAppTitleChange(getString(R.string.tab_installed) + " (" + items.size() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
