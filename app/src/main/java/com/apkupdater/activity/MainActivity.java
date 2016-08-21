package com.apkupdater.activity;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.apkupdater.adapter.MainActivityPageAdapter;
import com.apkupdater.R;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.UpdaterTitleChange;
import com.apkupdater.service.AlarmReceiver_;
import com.apkupdater.service.BootReceiver_;
import com.apkupdater.service.UpdaterService_;
import com.apkupdater.util.MyBus;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EActivity(R.layout.activity_main)
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

	// Not sure how safe this is...maybe save it on saveInstanceState
	static boolean mFirstStart = true;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		selectTab(intent.getIntExtra("tab", 0));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		// Check the intent to see if we are starting from notification
		Intent intent = getIntent();
		int startTab = 0;
		if (intent != null) {
			startTab = intent.getIntExtra("tab", -1);
			if (startTab == -1) {
				startTab = 0;
			} else {
				mFirstStart = false;
			}
		}

		if (mFirstStart) {
			clearResultsFromSharedPrefs();

			// Simulate a boot receiver to set alarm
			BootReceiver_ receiver = new BootReceiver_();
			receiver.onReceive(getBaseContext(), null);

			mFirstStart = false;
		}

		mBus.register(this);
		setSupportActionBar(mToolbar);
		mViewPager.setAdapter(new MainActivityPageAdapter(getBaseContext(), getSupportFragmentManager()));
		mViewPager.setOffscreenPageLimit(2);
		mTabLayout.setupWithViewPager(mViewPager);
		selectTab(startTab);
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
		SettingsActivity_.intent(this).start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@OptionsItem(R.id.action_update)
	void onUpdateClick(
	) {
		UpdaterService_.intent(getApplication()).start();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void clearResultsFromSharedPrefs(
	) {
		SharedPreferences.Editor editor = getBaseContext().getSharedPreferences("updates", MODE_PRIVATE).edit();
		editor.remove("updates");
		editor.commit();
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
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
