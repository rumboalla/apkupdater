package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.apkupdater.adapter.MainActivityPageAdapter;
import com.apkupdater.R;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.fragment.SettingsFragment;
import com.apkupdater.receiver.BootReceiver_;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ThemeUtil;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import com.apkupdater.model.AppState;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EActivity
@OptionsMenu(R.menu.menu_main)
public class MainActivity
	extends AppCompatActivity
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.container)
	ViewPager mViewPager;

	@ViewById(R.id.toolbar)
	Toolbar mToolbar;

	@ViewById(R.id.tabs)
	TabLayout mTabLayout;

	@Bean
	MyBus mBus;

	@Bean
	AppState mAppState;

	@ViewById(R.id.settings_container)
	FrameLayout mSettingsLayout;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(
		Bundle savedInstanceState
	) {
		setThemeFromOptions();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Select tab
		selectTab(mAppState.getSelectedTab());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		checkFirstStart();
		mBus.register(this);
		setSupportActionBar(mToolbar);
		mViewPager.setAdapter(new MainActivityPageAdapter(getBaseContext(), getSupportFragmentManager()));
		mViewPager.setOffscreenPageLimit(2);
		mTabLayout.setupWithViewPager(mViewPager);
		mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override public void onTabSelected(TabLayout.Tab tab) { mAppState.setSelectedTab(tab.getPosition()); }
			@Override public void onTabUnselected(TabLayout.Tab tab) {}
			@Override public void onTabReselected(TabLayout.Tab tab) {}
		});
		selectTab(mAppState.getSelectedTab());

		if (Build.VERSION.SDK_INT <= 11) { // Fix for 2.3 background not being set properly for some reason
			getWindow().getDecorView().setBackgroundColor(
				ColorUtitl.getColorFromTheme(getTheme(), android.R.attr.windowBackground)
			);
		}

		// Add the settings fragment and configure the correct state
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit();
		switchSettings(mAppState.getSettingsActive());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchSettings(
		boolean b
	) {
		if (b) {
			mTabLayout.setVisibility(View.GONE);
			mViewPager.setVisibility(View.GONE);
			mSettingsLayout.setVisibility(View.VISIBLE);
			getSupportActionBar().setTitle(getString(R.string.action_settings));
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		} else {
			mTabLayout.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.VISIBLE);
			mSettingsLayout.setVisibility(View.GONE);
			getSupportActionBar().setTitle(getString(R.string.app_name));
			getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		}

		mAppState.setSettingsActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void checkFirstStart(
	) {
		if (mAppState.getFirstStart()) {
			// Remove any stored updates we had and reset tab position
			mAppState.clearUpdates();
			mAppState.setSelectedTab(0);

			// Simulate a boot com.apkupdater.receiver to set alarm
			new BootReceiver_().onReceive(getBaseContext(), null);

			// Set the first start flag to false
			mAppState.setFirstStart(false);
			mAppState.setSettingsActive(false);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void selectTab(
		int tab
	) {
		try {
			mTabLayout.getTabAt(tab).select();
		} catch (Exception ignored) {

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_settings)
	void onSettingsClick(
	) {
		switchSettings(!mAppState.getSettingsActive());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_update)
	void onUpdateClick(
	) {
		UpdaterService_.intent(getApplication()).start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(android.R.id.home)
	void onHomeClick(
	) {
		switchSettings(!mAppState.getSettingsActive());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBus.unregister(this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onInstalledAppTitleChange(
		InstalledAppTitleChange t
	) {
		try {
			mTabLayout.getTabAt(0).setText(t.getTitle());
		} catch (Exception ignored) {

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdaterTitleChange(
		UpdaterTitleChange t
	) {
		try {
			mTabLayout.getTabAt(1).setText(t.getTitle());
		} catch (Exception ignored) {

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setThemeFromOptions(
	) {
		int theme = ThemeUtil.getActivityThemeFromOptions(getBaseContext());
		mAppState.setCurrentTheme(theme);
		setTheme(theme);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onResume() {
		super.onResume();

		// We are checking if the theme changed
		if (mAppState.getCurrentTheme() != ThemeUtil.getActivityThemeFromOptions(getBaseContext())) {
			mAppState.setSelectedTab(mTabLayout.getSelectedTabPosition());
			finish();
			MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
		}

		// Select tab
		selectTab(mAppState.getSelectedTab());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
