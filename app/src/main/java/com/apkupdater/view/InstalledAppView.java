package com.apkupdater.view;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.installedapp.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EViewGroup(R.layout.installed_app_item)
public class InstalledAppView
	extends LinearLayout
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.installed_app_name)
	TextView mName;

	@ViewById(R.id.installed_app_pname)
	TextView mPname;

	@ViewById(R.id.installed_app_version)
	TextView mVersion;

	@ViewById(R.id.installed_app_icon)
	ImageView mIcon;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public InstalledAppView(
		Context context
	) {
		super(context);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void bind(
		InstalledApp app
	) {
		mName.setText(app.getName());
		mPname.setText(app.getPname());
		mVersion.setText(app.getVersion());

		// Make the ignore overlay visible if this app is on the ignore list
		UpdaterOptions options = new UpdaterOptions(getContext());
		if (options.getIgnoreList().contains(app.getPname())) {
			if (android.os.Build.VERSION.SDK_INT >= 11) { // No alpha for old versions
				setAlpha(0.50f);
			}
		} else {
			if (android.os.Build.VERSION.SDK_INT >= 11) { // No alpha for old versions
				setAlpha(1.0f);
			}
		}

		try {
			Drawable icon = getContext().getPackageManager().getApplicationIcon(app.getPname());
			mIcon.setImageDrawable(icon);
		} catch (PackageManager.NameNotFoundException ignored) {

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////