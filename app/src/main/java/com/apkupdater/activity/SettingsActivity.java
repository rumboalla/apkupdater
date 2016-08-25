package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.apkupdater.R;
import com.apkupdater.util.AlarmUtil;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.ThemeUtil;

import org.androidannotations.annotations.EActivity;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EActivity
public class SettingsActivity
	extends PreferenceActivityCompat
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	SharedPreferences.OnSharedPreferenceChangeListener mChanges = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(
			SharedPreferences sharedPreferences,
			String key
		) {
			// Change alarm
			if (key.equals(getString(R.string.preferences_general_alarm_key))) {
				AlarmUtil alarmUtil = new AlarmUtil(mContext);
				alarmUtil.setAlarmFromOptions();
			} else if (key.equals(getString(R.string.preferences_general_theme_key))) {
				changeTheme();
			}
		}
	};

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onOptionsItemSelected(
		MenuItem menuItem
	) {
		// Exit activity if click on arrow
		if (menuItem.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(menuItem);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void setupToolbar(
	) {
		ViewGroup rootView = (ViewGroup)findViewById(R.id.action_bar_root);

		if (rootView != null) {
			View view = getLayoutInflater().inflate(R.layout.app_toolbar, rootView, false);
			rootView.addView(view, 0);

			// Configure the toolbar
			Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
			toolbar.setTitle(getString(R.string.action_settings));
			toolbar.setTitleTextColor(ColorUtitl.getColorFromTheme(getTheme(), R.attr.tabIndicatorColor));
			setSupportActionBar(toolbar);
		}

		// Display back arrow
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
			upArrow.setColorFilter(ColorUtitl.getColorFromTheme(getTheme(), R.attr.tabIndicatorColor), PorterDuff.Mode.SRC_ATOP);
			getSupportActionBar().setHomeAsUpIndicator(upArrow);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@MainThread
	private void changeTheme(
	) {
		SettingsActivity_.intent(this).flags(FLAG_ACTIVITY_CLEAR_TOP).start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(
		Bundle savedInstanceState
	) {
		mContext = getBaseContext();
		setTheme(ThemeUtil.getSettingsThemeFromOptions(mContext));
		super.onCreate(savedInstanceState);
		setupToolbar();
		PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(mChanges);
		addPreferencesFromResource(R.xml.preferences);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////