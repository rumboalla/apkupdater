package com.apkcrawler.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.apkcrawler.R;
import com.apkcrawler.activity.MainActivity_;
import com.apkcrawler.model.AppState;
import com.apkcrawler.model.DownloadInfo;
import com.apkcrawler.util.DownloadUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EReceiver
public class SelfUpdateNotificationReceiver
	extends BroadcastReceiver
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    AppState mAppState;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onReceive(
		Context context,
		Intent intent
	) {
	    try {
            String url = intent.getStringExtra("url");
            String versionName = intent.getStringExtra("versionName");

            MainActivity_.intent(context).flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP).start();

            String text = String.format("%s %s", context.getString(R.string.app_name), versionName);
            long id = DownloadUtil.downloadFile(context, url, "", text);

            // Add to the DownloadInfo
            mAppState.getDownloadInfo().put(
                id,
                new DownloadInfo(context.getPackageName(), 0, versionName)
            );
        } catch (Exception e) {
	        e.printStackTrace();
        }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////