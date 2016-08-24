package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.apkupdater.R;
import com.apkupdater.adapter.UpdaterAdapter;
import com.apkupdater.event.UpdateFinalProgressEvent;
import com.apkupdater.event.UpdateProgressEvent;
import com.apkupdater.event.UpdateStartEvent;
import com.apkupdater.event.UpdateStopEvent;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.installedapp.InstalledAppUtil;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.updater.Update;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;
import com.apkupdater.util.SnackBarUtil;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.List;

import com.apkupdater.model.AppState;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_updater)
public class UpdaterFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	ListView mListView;

	@Bean
	UpdaterAdapter mAdapter;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@ViewById(R.id.loader)
	ProgressBar mProgressBar;

	@ColorRes(R.color.colorPrimary)
	int mPrimaryColor;

	@Bean
	MyBus mBus;

	@Bean
	AppState mAppState;

	private Bundle mSavedInstance;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void onCreate (
		Bundle savedInstanceState
	) {
		mSavedInstance = savedInstanceState;
		super.onCreate(savedInstanceState);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void addUpdateToList(
		Update update
	) {
		try {
			mAdapter.add(update);
			sendUpdateTitleEvent();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void setProgressBarVisibility(
		int v
	) {
		try {
			mProgressBar.setVisibility(v);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ItemClick(R.id.list_view)
	void onUpdateClicked(Update u) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(u.getUrl()));
		startActivity(browserIntent);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateStartEvent(
		UpdateStartEvent ev
	) {
		mAdapter.clear();
		sendUpdateTitleEvent();
		setProgressBarVisibility(View.VISIBLE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateStopEvent(
		UpdateStopEvent ev
	) {
		setProgressBarVisibility(INVISIBLE);
		String m = ev.getMessage();
		if (m != null && !m.isEmpty()) {
			SnackBarUtil.make(getActivity(), m);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateFinalProgresEvent(
		UpdateFinalProgressEvent ev
	) {
		List<Update> updates = ev.getUpdates();
		if (mAdapter.getValues().length < updates.size()) {
			mAdapter.clear();

			for (Update i : updates) { // addAll needs API level 11+
				mAdapter.add(i);
			}

			sendUpdateTitleEvent();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateProgressEvent(
		UpdateProgressEvent ev
	) {
		addUpdateToList(ev.getUpdate());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onStop() {
		mBus.unregister(this);
		super.onStop();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onStart() {
		super.onStart();
		mBus.register(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onResume() {
		super.onResume();
		loadData();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void sendUpdateTitleEvent(
	) {
		mBus.post(new UpdaterTitleChange(getString(R.string.tab_updates) + " (" + mAdapter.getCount() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private boolean loadData(
	) {
		mAdapter.clear();

		// Get the updates and add them to the adapter
		List<Update> updates = mAppState.getUpdates();
		if (!updates.isEmpty()) {
			for (Update i : updates) { // addAll needs API level 11+
				mAdapter.add(i);
			}
			sendUpdateTitleEvent();
			setProgressBarVisibility(INVISIBLE);
			return true;
		}

		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		// Set the correct color for the ProgressBar
		mProgressBar.setIndeterminate(true);
		mProgressBar.getIndeterminateDrawable().setColorFilter(ColorUtitl.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent), android.graphics.PorterDuff.Mode.MULTIPLY);

		// Load data
		loadData();

		// Change the visibility of the loader based on the service status
		if (ServiceUtil.isServiceRunning(getContext(), UpdaterService_.class)) {
			setProgressBarVisibility(VISIBLE);
		} else {
			setProgressBarVisibility(INVISIBLE);
		}

		mListView.setAdapter(mAdapter);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////