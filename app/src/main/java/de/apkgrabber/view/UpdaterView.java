package de.apkgrabber.view;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.apkgrabber.R;
import de.apkgrabber.model.Update;

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

	@ViewById(R.id.action_one_button)
	Button mActionOneButton;

	@ViewById(R.id.action_two_button)
	Button mActionTwoButton;

	Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterView(
		Context context
	) {
		super(context);
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void bind(
		Update update
	) {
		mName.setText(update.getName());
		mPname.setText(update.getPname());

		// Build version string with both old and new version
		String version = update.getVersion();
		if (update.getNewVersion() != null && !update.getNewVersion().isEmpty()) {
			version += " -> " + update.getNewVersion();
		}

		mVersion.setText(version);

		// Build string for first action
		String action = "";
		if (update.getUrl().contains("apkmirror.com")) {
			action = mContext.getString(R.string.action_apkmirror);
		} else if (update.getUrl().contains("uptodown.com")) {
			action = mContext.getString(R.string.action_uptodown);
		} else if (update.getUrl().contains("apkpure.com")) {
			action = mContext.getString(R.string.action_apkpure);
		} else if(update.getUrl().contains("aptoide.com")) {
			action = mContext.getString(R.string.action_aptoide);
		}

		mActionOneButton.setText(action);

		try {
			Drawable icon = getContext().getPackageManager().getApplicationIcon(update.getPname());
			mIcon.setImageDrawable(icon);
		} catch (PackageManager.NameNotFoundException ignored) {}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setActionOneButtonListener(
		View.OnClickListener listener
	) {
		mActionOneButton.setOnClickListener(listener);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setActionTwoButtonListener(
		View.OnClickListener listener
	) {
		mActionTwoButton.setOnClickListener(listener);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////