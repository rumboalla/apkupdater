package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.fragment.LogFragment_;
import com.apkupdater.fragment.MainFragment;
import com.apkupdater.fragment.MainFragment_;
import com.apkupdater.fragment.SettingsFragment_;
import com.apkupdater.model.AppState;
import com.apkupdater.receiver.BootReceiver_;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ThemeUtil;

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

	@ViewById(R.id.toolbar)
	Toolbar mToolbar;

	@Bean
	MyBus mBus;

	@Bean
	AppState mAppState;

	@ViewById(R.id.container)
	FrameLayout mContainer;

	SettingsFragment_ mSettingsFragment;
	LogFragment_ mLogFragment;
	MainFragment_ mMainFragment;

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

	@AfterViews
	void init(
	) {
		checkFirstStart();
		mBus.register(this);
		setSupportActionBar(mToolbar);

		// Create fragments
		mMainFragment = new MainFragment_();
		mSettingsFragment = new SettingsFragment_();
		mLogFragment = new LogFragment_();

		// Add the main fragment and configure the correct state
		if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof MainFragment)) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, mMainFragment).commit();
		}

		// Switch to the correct fragment
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
		if (b) {
			replaceFragment(mSettingsFragment, true);
			changeToolbar(getString(R.string.action_settings), true);
		} else {
			replaceFragment(mMainFragment, false);
			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setSettingsActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchLog(
		boolean b
	) {
		if (b) {
			replaceFragment(mLogFragment, true);
			changeToolbar(getString(R.string.action_log), true);
		} else {
			replaceFragment(mMainFragment, false);
			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setLogActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void replaceFragment(
		Fragment f,
		boolean in
	) {
		if (in) {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.replace(R.id.container, f)
			.commit();
		} else {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.replace(R.id.container, f)
			.commit();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void changeToolbar(
		String title,
		boolean arrow
	) {
		try {
		ActionBar bar = getSupportActionBar();
			if (bar != null) {
				if (Build.VERSION.SDK_INT > 13) {
					TransitionManager.beginDelayedTransition(mToolbar, new AutoTransition().setDuration(250));
				}

				// This is to try to avoid the text to be cut during animation. TODO: Find a better way.
				TextView t = (TextView) mToolbar.getChildAt(0);
				t.getLayoutParams().width = 2000;

				bar.setTitle(title);
				bar.setDisplayHomeAsUpEnabled(arrow);
			}
		} catch (Exception e) {}
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
			finish();
			MainActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_CLEAR_TOP).start();
		}
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
