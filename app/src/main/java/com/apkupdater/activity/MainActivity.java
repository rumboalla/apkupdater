package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;
import com.apkupdater.util.ThemeUtil;

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
		super.onCreate(savedInstanceState);

		// Set theme and set activity content and toolbar
		setThemeFromOptions();
		setContentView(R.layout.activity_main);
		setSupportActionBar(mToolbar);

		mBus.register(this);
		mAppState.clearUpdates();

		// Simulate a boot com.apkupdater.receiver to set alarm
		new BootReceiver_().onReceive(getBaseContext(), null);

		// Create fragments
		mMainFragment = new MainFragment_();
		mSettingsFragment = new SettingsFragment_();
		mLogFragment = new LogFragment_();

		// Add the main fragment
		if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof MainFragment)) {
			getSupportFragmentManager().beginTransaction()
				.add(R.id.container, mMainFragment)
				.add(R.id.container, mSettingsFragment)
				.add(R.id.container, mLogFragment)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
			.commit();
		}

		// Switch to the correct fragment
		if (mAppState.getSettingsActive()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					switchSettings(true);
				}
			}, 1);
		} else if (mAppState.getLogActive()) {
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					switchLog(true);
				}
			}, 1);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchSettings(
		boolean b
	) {
		if (b) {
			//replaceFragment(mSettingsFragment, true);

			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.show(mSettingsFragment)
				.hide(mMainFragment)
				.hide(mLogFragment)
			.commit();

			changeToolbar(getString(R.string.action_settings), true);
		} else {
			//replaceFragment(mMainFragment, false);

			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
			.commit();

			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setSettingsActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchLog(
		boolean b
	) {
		if (b) {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.show(mLogFragment)
				.hide(mMainFragment)
				.hide(mSettingsFragment)
				.commit();

			changeToolbar(getString(R.string.action_log), true);
		} else {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.commit();

			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setLogActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void changeToolbar(
		String title,
		boolean arrow
	) {
		try {
		ActionBar bar = getSupportActionBar();
			if (bar != null) {
				AnimationUtil.startToolbarAnimation(mToolbar);

				// This is to try to avoid the text to be cut during animation. TODO: Find a better way.
				TextView t = (TextView) mToolbar.getChildAt(0);
				t.getLayoutParams().width = 2000;

				bar.setTitle(title);
				bar.setDisplayHomeAsUpEnabled(arrow);
			}
		} catch (Exception e) {}
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
		if (!ServiceUtil.isServiceRunning(getBaseContext(), UpdaterService_.class)) {
			UpdaterService_.intent(getApplication()).start();
		}
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
