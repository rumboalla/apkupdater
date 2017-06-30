package com.apkupdater.view;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.LogMessage;
import com.apkupdater.util.ColorUtil;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
		int color = ColorUtil.getColorFromContext(getContext(), android.R.attr.textColorTertiary);

		mTitle.setText(message.getTitle());
		//mTitle.setTextColor(color);

		mMessage.setText(message.getMessage());
		//mMessage.setTextColor(color);

		DateFormat df = SimpleDateFormat.getDateTimeInstance();
		mTime.setText(df.format(new Date(message.getTime())));
		//mTime.setTextColor(color);

		if (message.getSeverity() == LogMessage.SEVERITY_INFO) {
            mIcon.setImageResource(R.drawable.ic_info);
		} else if  (message.getSeverity() == LogMessage.SEVERITY_WARNING) {
            mIcon.setImageResource(R.drawable.ic_warning);
		} else if  (message.getSeverity() == LogMessage.SEVERITY_ERROR) {
			mIcon.setImageResource(R.drawable.ic_error);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////