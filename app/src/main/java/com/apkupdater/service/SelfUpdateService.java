package com.apkupdater.service;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.apkupdater.R;
import com.apkupdater.model.AppState;
import com.apkupdater.model.GitHub.Release;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.VersionUtil;
import com.google.gson.Gson;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EService
public class SelfUpdateService
	extends IntentService
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static private final String BaseUrl = "https://api.github.com/";
    static private final String LatestUrl = "repos/rumboalla/apkupdater/releases/latest";
    static private final String AcceptHeader = "application/vnd.github.v3+json";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean
	MyBus mBus;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	AppState mAppState;

	@Bean
	LogUtil mLogger;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public SelfUpdateService(
	) {
		super(SelfUpdateService.class.getSimpleName());
	}

	private void doNotification(
	    String newVersion,
        String apkUrl
    ) {
	    Context c = getApplicationContext();
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

        b.setContentTitle(String.format(c.getString(R.string.selfupdate_update), newVersion));
        b.setContentText((c.getString(R.string.selfupdate_click_to_install)));
        b.setSmallIcon(R.drawable.ic_update);
        b.setAutoCancel(true);
        b.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher));

        // Set the click intent
        Intent intent = new Intent("com.apkupdater.selfupdatenotification");
        intent.setFlags(0);
        intent.putExtra("url", apkUrl);
        intent.putExtra("name", String.format("%s %s", c.getString(R.string.app_name), newVersion));
        b.setContentIntent(PendingIntent.getBroadcast(c, new Random().nextInt(), intent, 0));

        // Launch notification
        NotificationManager m = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        m.notify(42 + 1, b.build());
    }

	private void checkForUpdate(
    ) {
	    try {
            OkHttpClient client = new OkHttpClient.Builder().build();

            Request request = new Request.Builder()
                .url(BaseUrl + LatestUrl)
                .header("Accept", AcceptHeader)
                .get()
                .build();

            Response response = client.newCall(request).execute();

            Release r = new Gson().fromJson(response.body().string(), Release.class);


            int c = VersionUtil.compareVersion(
                VersionUtil.getVersionFromString(getPackageManager().getPackageInfo(getPackageName(), 0).versionName),
                VersionUtil.getVersionFromString(r.getTagName())
            );

            if (c < 0) {
                doNotification(r.getTagName(), r.getAssets().get(0).getBrowserDownloadUrl());
            }
        } catch (Exception e) {
	        // Error checking for update
        }
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onHandleIntent(
		Intent intent
	) {
        checkForUpdate();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
