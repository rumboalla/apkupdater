package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.apkupdater.R;
import com.apkupdater.adapter.MainActivityPageAdapter;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.fragment.LogFragment_;
import com.apkupdater.fragment.SettingsFragment;
import com.apkupdater.fragment.SettingsFragment_;
import com.apkupdater.model.AppState;
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

	@ViewById(R.id.log_container)
	FrameLayout mLogLayout;

	SettingsFragment_ mSettingsFragment;
	LogFragment_ mLogFragment;

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
		mSettingsFragment = new SettingsFragment_();
		if (!(getSupportFragmentManager().findFragmentById(R.id.settings_container) instanceof SettingsFragment)) {
			getSupportFragmentManager().beginTransaction().add(R.id.settings_container, mSettingsFragment).commit();
		}

		mLogFragment = new LogFragment_();
		if (!(getSupportFragmentManager().findFragmentById(R.id.log_container) instanceof LogFragment_)) {
			getSupportFragmentManager().beginTransaction().add(R.id.log_container, mLogFragment).commit();
		}

		if (mAppState.getSettingsActive()) {
			switchSettings(true);
		} else if (mAppState.getLogActive()) {
			switchLog(true);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchSettings(
		boolean b
	) {
        ActionBar bar = getSupportActionBar();
		if (b) {
			mTabLayout.setVisibility(View.GONE);
			mViewPager.setVisibility(View.GONE);
			mSettingsLayout.setVisibility(View.VISIBLE);
			mLogLayout.setVisibility(View.GONE);

			if (bar != null) {
                bar.setDisplayHomeAsUpEnabled(true);
                bar.setTitle(getString(R.string.action_settings));
            }
		} else {
			mTabLayout.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.VISIBLE);
			mSettingsLayout.setVisibility(View.GONE);
			mLogLayout.setVisibility(View.GONE);
            if (bar != null) {
                bar.setTitle(getString(R.string.app_name));
                bar.setDisplayHomeAsUpEnabled(false);
            }
		}

		mAppState.setSettingsActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchLog(
		boolean b
	) {
        ActionBar bar = getSupportActionBar();
		if (b) {
			mTabLayout.setVisibility(View.GONE);
			mViewPager.setVisibility(View.GONE);
			mSettingsLayout.setVisibility(View.GONE);
			mLogLayout.setVisibility(View.VISIBLE);

            if (bar != null) {
                bar.setDisplayHomeAsUpEnabled(true);
                bar.setTitle(getString(R.string.action_log));
            }
		} else {
			mTabLayout.setVisibility(View.VISIBLE);
			mViewPager.setVisibility(View.VISIBLE);
			mSettingsLayout.setVisibility(View.GONE);
			mLogLayout.setVisibility(View.GONE);
            if (bar != null) {
                bar.setTitle(getString(R.string.app_name));
                bar.setDisplayHomeAsUpEnabled(false);
            }
		}

		mAppState.setLogActive(b);
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
        TabLayout.Tab selectedTab = mTabLayout.getTabAt(tab);
        if (selectedTab != null) {
            selectedTab.select();
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
		if (mAppState.getSettingsActive()) {
			switchSettings(!mAppState.getSettingsActive());
		} else if (mAppState.getLogActive()) {
			switchLog(!mAppState.getLogActive());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_log)
	void onLogClick(
	) {
		switchLog(!mAppState.getLogActive());
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
        TabLayout.Tab selectedTab = mTabLayout.getTabAt(0);
        if (selectedTab != null){
            selectedTab.setText(t.getTitle());
        }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
	public void onUpdaterTitleChange(
		UpdaterTitleChange t
	) {
        TabLayout.Tab selectedTab = mTabLayout.getTabAt(1);
        if (selectedTab != null) {
            selectedTab.setText(t.getTitle());
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

	@Override
	public void onBackPressed() {
		// Handle back press depending on app state
		if (mAppState.getLogActive()) {
			switchLog(false);
		} else if (mAppState.getSettingsActive()){
			switchSettings(false);
		} else {
			super.onBackPressed();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
