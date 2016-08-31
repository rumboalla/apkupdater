package com.apkupdater.view;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.updater.UpdaterOptions;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EViewGroup(R.layout.log_item)
public class LogView
	extends LinearLayout
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.log_title)
	TextView mTitle;

	@ViewById(R.id.log_time)
	TextView mTime;

	@ViewById(R.id.log_message)
	TextView mMessage;

	@ViewById(R.id.log_icon)
	ImageView mIcon;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public LogView(
		Context context
	) {
		super(context);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void bind(
		LogMessage message
	) {
		mTitle.setText(message.getTitle());
		mMessage.setText(message.getMessage());

		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		mTime.setText(df.format(new Date(message.getTime())));

		if (message.getSeverity() == LogMessage.SEVERITY_INFO) {
			mIcon.setContentDescription("Info");
		} else if  (message.getSeverity() == LogMessage.SEVERITY_WARNING) {
			mIcon.setContentDescription("Warning");
		} else if  (message.getSeverity() == LogMessage.SEVERITY_ERROR) {
			mIcon.setImageResource(R.drawable.ic_error);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////