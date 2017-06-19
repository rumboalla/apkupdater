package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.fragment.AboutFragment_;
import com.apkupdater.fragment.LogFragment_;
import com.apkupdater.fragment.MainFragment;
import com.apkupdater.fragment.MainFragment_;
import com.apkupdater.fragment.SettingsFragment_;
import com.apkupdater.model.AppState;
import com.apkupdater.receiver.BootReceiver_;
import com.apkupdater.receiver.DownloadReceiver;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.ColorUtil;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ServiceUtil;
import com.apkupdater.util.ThemeUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EActivity
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

	@ViewById(R.id.update_button)
    FloatingActionButton mUpdateButton;

	Menu mMenu;
	SettingsFragment_ mSettingsFragment;
	AboutFragment_ mAboutFragment;
	LogFragment_ mLogFragment;
	MainFragment_ mMainFragment;

	DownloadReceiver downloadReceiver;

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

		// Clear updates unless we are coming from a notification
		boolean isFromNotification = false;
		try {
			isFromNotification = getIntent().getExtras().getBoolean("isFromNotification");
		} catch (Exception ignored) {}

		if (!isFromNotification) {
			//mAppState.clearUpdates();
		}

		// Simulate a boot com.apkupdater.receiver to set alarm
		new BootReceiver_().onReceive(getBaseContext(), null);

		// Create fragments
		mMainFragment = new MainFragment_();
		mSettingsFragment = new SettingsFragment_();
		mLogFragment = new LogFragment_();
		mAboutFragment = new AboutFragment_();

		// Add the main fragment
		if (!(getSupportFragmentManager().findFragmentById(R.id.container) instanceof MainFragment)) {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, mMainFragment)
				.add(R.id.container, mSettingsFragment)
				.add(R.id.container, mLogFragment)
				.add(R.id.container, mAboutFragment)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.hide(mAboutFragment)
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
		} else if (mAppState.getAboutActive()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switchAbout(true);
                }
            }, 1);
        }

		// Download receiver
        DownloadUtil.deleteDownloadedFiles(this);
        downloadReceiver = new DownloadReceiver();
        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Tint floating action button
        mUpdateButton.setImageDrawable(ColorUtil.tintDrawable(this, mUpdateButton.getDrawable(), android.R.attr.textColorPrimary));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchSettings(
		boolean b
	) {
		if (b) {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.show(mSettingsFragment)
				.hide(mMainFragment)
				.hide(mLogFragment)
				.hide(mAboutFragment)
			.commit();
            setUpdateButtonVisibility(false);
			changeToolbar(getString(R.string.action_settings), true);
		} else {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.hide(mAboutFragment)
			.commit();

            setUpdateButtonVisibility(true);
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
				.hide(mAboutFragment)
			.commit();
            setUpdateButtonVisibility(false);
			changeToolbar(getString(R.string.action_log), true);
		} else {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.hide(mAboutFragment)
			.commit();
            setUpdateButtonVisibility(true);
			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setLogActive(b);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void switchAbout(
		boolean b
	) {
		if (b) {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left)
				.show(mAboutFragment)
				.hide(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.commit();
            setUpdateButtonVisibility(false);
			changeToolbar(getString(R.string.tab_about), true);
		} else {
			getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right)
				.show(mMainFragment)
				.hide(mSettingsFragment)
				.hide(mLogFragment)
				.hide(mAboutFragment)
				.commit();
            setUpdateButtonVisibility(true);
			changeToolbar(getString(R.string.app_name), false);
		}

		mAppState.setAboutActive(b);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_settings).setIcon(
            ColorUtil.tintDrawable(this, menu.findItem(R.id.action_settings).getIcon(), android.R.attr.textColorPrimary)
        );

        return super.onCreateOptionsMenu(menu);
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(android.R.id.home)
	void onHomeClick(
	) {
		if (mAppState.getSettingsActive()) {
			switchSettings(!mAppState.getSettingsActive());
		} else if (mAppState.getLogActive()) {
			switchLog(!mAppState.getLogActive());
		} else if (mAppState.getAboutActive()) {
			switchAbout(!mAppState.getAboutActive());
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_log)
	void onLogClick(
	) {
		switchLog(!mAppState.getLogActive());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_about)
	void onAboutClick(
	) {
		switchAbout(!mAppState.getAboutActive());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBus.unregister(this);
		unregisterReceiver(downloadReceiver);
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
		} else if (mAppState.getAboutActive()){
			switchAbout(false);
		} else {
			super.onBackPressed();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Click(R.id.update_button)
    protected void onUpdateClick(
    ) {
        if (!ServiceUtil.isServiceRunning(getBaseContext(), UpdaterService_.class)) {
            UpdaterService_.intent(getApplication()).start();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void setUpdateButtonVisibility(
        boolean visible
    ) {
        mUpdateButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
