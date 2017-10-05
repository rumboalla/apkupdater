package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.activity.MainActivity;
import com.apkupdater.adapter.UpdaterAdapter;
import com.apkupdater.event.RefreshUpdateTitle;
import com.apkupdater.event.UpdateFinalProgressEvent;
import com.apkupdater.event.UpdateProgressEvent;
import com.apkupdater.event.UpdateStartEvent;
import com.apkupdater.event.UpdateStopEvent;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.model.AppState;
import com.apkupdater.model.Update;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.ColorUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
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
import static android.view.View.VISIBLE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_updater)
public class UpdaterFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	RecyclerView mRecyclerView;

	@ViewById(R.id.swipe_refresh_layout)
	SwipeRefreshLayout mSwipeRefreshLayout;

	@ViewById(R.id.container)
	LinearLayout mContainer;

	@ViewById(R.id.loader_container)
	CardView mLoaderContainer;

	@ViewById(R.id.loader_text)
	TextView mLoaderText;

    @ViewById(R.id.no_updates_text)
    TextView mNoUpdatesText;

	@ViewById(R.id.loader)
	ProgressBar mLoader;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@ColorRes(R.color.colorPrimary)
	int mPrimaryColor;

	@Bean
	MyBus mBus;

	@Bean
	AppState mAppState;

	UpdaterAdapter mAdapter;

	private int mProgressCount = 0;
	private int mProgressMax = 0;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void onCreate (
		Bundle savedInstanceState
	) {
		super.onCreate(savedInstanceState);
		mAdapter = new UpdaterAdapter(getContext());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	void initProgressBar(
	) {
		try {
			mLoader.getIndeterminateDrawable().setColorFilter(
				ColorUtil.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent),
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
			AnimationUtil.startSlideAnimation(getContext(), mLoaderContainer);
			mLoaderContainer.setVisibility(v);
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
			AnimationUtil.startSlideAnimation(getContext(), mLoaderContainer);

			// Change progress bar
			mLoader.setMax(max);
			mLoader.setProgress(progress);
			if (progress == 0 && max == 0) {
				mLoader.setIndeterminate(true);
			} else {
				mLoader.setIndeterminate(false);
			}

			// Change text
			mLoaderText.setText(progress + "/" + max);
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
		mNoUpdatesText.setVisibility(GONE);
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

		if (mAdapter.getItemCount() < updates.size()) {
			mAdapter.setUpdates(updates);
		}
        if (updates.isEmpty()) {
		    mNoUpdatesText.setVisibility(VISIBLE);
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

	@Subscribe
	public void onRefreshUpdateTitle(
		RefreshUpdateTitle ev
	) {
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
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void sendUpdateTitleEvent(
	) {
		mBus.post(new UpdaterTitleChange(getString(R.string.tab_updates) + " (" + mAdapter.getItemCount() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void loadData(
	) {
		// Check if we are updating
		mProgressCount = mAppState.getUpdateProgress();
		mProgressMax = mAppState.getUpdateMax();

		// Get the updates and add them to the adapter
		List<Update> updates = mAppState.getUpdates();
		if (!updates.isEmpty()) {
			mAdapter.setUpdates(updates);
			sendUpdateTitleEvent();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
        mAdapter.init(mRecyclerView, new ArrayList<Update>());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		if (new UpdaterOptions(getContext()).disableAnimations()) {
		    mRecyclerView.setItemAnimator(null);
        } else {
            ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
		mRecyclerView.setAdapter(mAdapter);

		// Load data
		loadData();

		initProgressBar();

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				((MainActivity)getActivity()).onUpdateClick();
				mSwipeRefreshLayout.setRefreshing(false);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////