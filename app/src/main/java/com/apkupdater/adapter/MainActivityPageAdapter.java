package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.apkupdater.fragment.InstalledAppFragment_;
import com.apkupdater.fragment.SearchFragment;
import com.apkupdater.fragment.SearchFragment_;
import com.apkupdater.fragment.UpdaterFragment_;
import com.apkupdater.R;

import java.security.InvalidParameterException;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class MainActivityPageAdapter
	extends FragmentPagerAdapter
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private InstalledAppFragment_ mInstalledAppFragment = new InstalledAppFragment_();
	private UpdaterFragment_ mUpdaterFragment = new UpdaterFragment_();
	private SearchFragment mSearchFragment = new SearchFragment_();
	private Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public MainActivityPageAdapter(
		Context context,
		FragmentManager fm
	) {
		super(fm);
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Fragment getItem(
		int position
	) {
		if (position == 0) {
			return mInstalledAppFragment;
		} else if (position == 1){
			return mUpdaterFragment;
		} else if (position == 2){
			return mSearchFragment;
		} else {
			throw new InvalidParameterException("Invalid position.");
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getCount(
	) {
		return 3;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public CharSequence getPageTitle(
		int position
	) {
		if (position == 0) {
			return mContext.getString(R.string.tab_installed);
		} else if (position == 1){
			return mContext.getString(R.string.tab_updates);
		}  else if (position == 2){
			return mContext.getString(R.string.tab_search);
		} else {
			return "Error";
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
