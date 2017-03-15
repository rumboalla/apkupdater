package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.apkupdater.R;
import com.apkupdater.adapter.UpdaterAdapter;
import com.apkupdater.event.UpdateFinalProgressEvent;
import com.apkupdater.event.UpdateProgressEvent;
import com.apkupdater.event.UpdateStartEvent;
import com.apkupdater.event.UpdateStopEvent;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.model.AppState;
import com.apkupdater.model.Update;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;
import com.apkupdater.util.SnackBarUtil;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_updater)
public class UpdaterFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	RecyclerView mRecyclerView;

	@ViewById(R.id.container)
	LinearLayout mContainer;

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

	private int mProgressCount = 0;
	private int mProgressMax = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void onCreate (
		Bundle savedInstanceState
	) {
		super.onCreate(savedInstanceState);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void initProgressBar(
	) {
		try {
			mProgressBar.getIndeterminateDrawable().setColorFilter(
				ColorUtitl.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent),
				android.graphics.PorterDuff.Mode.MULTIPLY
			);
			setProgressBarProgress(mProgressCount, mProgressMax);
			if (mProgressMax == 0 && mProgressCount == 0) {
				setProgressBarVisibility(GONE);
			} else {
				setProgressBarVisibility(VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void setProgressBarVisibility(
		int v
	) {
		try {
			AnimationUtil.startListAnimation(mContainer);
			mProgressBar.setVisibility(v);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void setProgressBarProgress(
		int progress,
		int max
	) {
		try {
			AnimationUtil.startListAnimation(mContainer);
			mProgressBar.setMax(max);
			mProgressBar.setProgress(progress);
			if (progress == 0 && max == 0) {
				mProgressBar.setIndeterminate(true);
			} else {
				mProgressBar.setIndeterminate(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateStartEvent(
		UpdateStartEvent ev
	) {
		mAdapter.setUpdates(new ArrayList<Update>());
		sendUpdateTitleEvent();
		mProgressCount = 0;
		mProgressMax = ev.getNumUpdates();
		setProgressBarProgress(mProgressCount, mProgressMax);
		setProgressBarVisibility(View.VISIBLE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateStopEvent(
		UpdateStopEvent ev
	) {
		mProgressCount = 0;
		mProgressMax = 0;
		setProgressBarVisibility(GONE);
		setProgressBarProgress(mProgressCount, mProgressMax);
		String m = ev.getMessage();
		if (m != null && !m.isEmpty()) {
			SnackBarUtil.make(getActivity(), m);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateFinalProgressEvent(
		UpdateFinalProgressEvent ev
	) {
		List<Update> updates = ev.getUpdates();

		if (mAdapter.getCount() < updates.size()) {
			mAdapter.setUpdates(updates);
		}

		sendUpdateTitleEvent();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdateProgressEvent(
		UpdateProgressEvent ev
	) {
		mProgressCount++;
		setProgressBarProgress(mProgressCount, mProgressMax);
		if (ev.getUpdate() != null) {
			mAdapter.addUpdate(ev.getUpdate());
		}
		sendUpdateTitleEvent();
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
		// Get the updates and add them to the adapter
		List<Update> updates = mAppState.getUpdates();
		if (!updates.isEmpty()) {
			mAdapter.setUpdates(updates);
			sendUpdateTitleEvent();
			setProgressBarVisibility(GONE);
			return true;
		}

		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		initProgressBar();

		mAdapter = new UpdaterAdapter(getContext(), mRecyclerView, new ArrayList<Update>());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setAdapter(mAdapter);

		// Load data
		loadData();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////