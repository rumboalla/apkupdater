package com.apkupdater.view;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.updater.Update;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.ColorUtitl;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EViewGroup(R.layout.updater_item)
public class UpdaterView
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

	@ViewById(R.id.update_url)
	TextView mUrl;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterView(
		Context context
	) {
		super(context);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void bind(
		Update update
	) {
		mName.setText(update.getName());
		mPname.setText(update.getPname());
		mVersion.setText(update.getVersion());
		mUrl.setText(update.getUrl());
		mUrl.setTextColor(ColorUtitl.getColorFromTheme(getContext().getTheme(), R.attr.colorAccent));
		try {
			Drawable icon = getContext().getPackageManager().getApplicationIcon(update.getPname());
			mIcon.setImageDrawable(icon);
		} catch (PackageManager.NameNotFoundException ignored) {

		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////