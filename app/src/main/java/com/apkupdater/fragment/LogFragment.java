package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.ListView;

import com.apkupdater.R;
import com.apkupdater.adapter.LogAdapter;
import com.apkupdater.model.LogMessage;
import com.google.gson.Gson;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_installed_apps)
public class LogFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	ListView mListView;

	@Bean
	LogAdapter mAdapter;

	Bundle mSavedInstance;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(
		@Nullable Bundle savedInstanceState
	) {
		mSavedInstance = savedInstanceState;
		super.onCreate(savedInstanceState);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		// Try to get data from savedInstance
		if (mSavedInstance != null) {
			try {
				LogMessage[] messages = new Gson().fromJson(mSavedInstance.getString("values"), LogMessage[].class);
				for (LogMessage m : messages) {
					mAdapter.add(m);
				}
			} catch (Exception ignored) {
			}
		}

		mListView.setAdapter(mAdapter);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onSaveInstanceState(
		Bundle outState
	) {
		super.onSaveInstanceState(outState);

		// Serialize to json and put it on savedinstance
		outState.putString("values", new Gson().toJson(mAdapter.getValues()));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
