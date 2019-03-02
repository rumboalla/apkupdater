package de.apkgrabber.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.apkgrabber.R;
import de.apkgrabber.activity.MainActivity_;
import de.apkgrabber.model.AppState;
import de.apkgrabber.model.DownloadInfo;
import de.apkgrabber.util.DownloadUtil;

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