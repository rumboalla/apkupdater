package com.apkupdater.service;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import com.apkupdater.R;
import com.apkupdater.model.AppState;
import com.apkupdater.model.Constants;
import com.apkupdater.model.LogMessage;
import com.apkupdater.model.Update;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.FileUtil;
import com.apkupdater.util.GooglePlayUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.google.gson.Gson;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EService
public class AutomaticInstallerService
	extends IntentService
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Bean
	MyBus mBus;

	@Bean
	InstalledAppUtil mInstalledAppUtil;

	@Bean
	AppState mAppState;

	@Bean
	LogUtil mLogger;

	private int Status = 0;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public AutomaticInstallerService(
	) {
		super(AutomaticInstallerService.class.getSimpleName());
	}

	private void doNotification(
	    Notification notification
    ) {
        NotificationManager m = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (m != null) {
            m.notify(Constants.AutomaticUpdateNotificationId, notification);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void doAppNotification(
        int current,
        int max,
        Update u
    ) {
        Context c = getApplicationContext();
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

        b.setContentTitle(String.format(c.getString(R.string.automatic_installer_progress_title), current, max));
        b.setContentText(String.format(c.getString(R.string.automatic_installing), u.getPname()));
        b.setSmallIcon(R.drawable.ic_update);
        b.setAutoCancel(true);
        b.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher));
        b.setStyle(new NotificationCompat.BigTextStyle());

        // Launch notification
        doNotification(b.build());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void doErrorNotification(
    ) {
        Context c = getApplicationContext();
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

        b.setContentTitle(c.getString(R.string.automatic_installer_title));
        b.setContentText(c.getString(R.string.automatic_error));
        b.setSmallIcon(R.drawable.ic_update);
        b.setAutoCancel(true);
        b.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher));

        // Launch notification
        doNotification(b.build());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void doFinalNotification(
        List<Update> success,
        List<Update> failure
    ) {
        Context c = getApplicationContext();
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());

        // Build success string
        String s = "";
        if (success.size() > 0) {
            s = String.format(c.getString(R.string.automatic_successfully_installed), success.size());
            for (Update u : success) {
                s = s.concat(u.getPname() + ", ");
            }
            s = s.substring(0, s.length() - 2);
            s = s.concat(".");
        }

        // Build failure string
        String f = "";
        if (failure.size() > 0) {
            f = String.format(c.getString(R.string.automatic_failed_to_install), failure.size());
            for (Update u : failure) {
                f = f.concat(u.getPname() + ", ");
            }
            f = f.substring(0, f.length() - 2);
            f += ".";
        }

        b.setContentTitle(c.getString(R.string.automatic_installer_title));
        b.setContentText(String.format("%s\n%s", s, f));
        b.setSmallIcon(R.drawable.ic_update);
        b.setAutoCancel(true);
        b.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.mipmap.ic_launcher));
        b.setStyle(new NotificationCompat.BigTextStyle());

        // Launch notification
        doNotification(b.build());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void installApps(
	    Update [] updates
    ) {
	    try {
	        List<Update> success = new ArrayList<>(), failure = new ArrayList<>();
	        int i = 0;

            for (Update u : updates) {
                try {
                    i++;
                    doAppNotification(i, updates.length, u);

                    AndroidAppDeliveryData data = GooglePlayUtil.getAppDeliveryData(
                        GooglePlayUtil.getApi(getApplicationContext()),
                        u.getPname()
                    );

                    File f = DownloadUtil.downloadFile(
                        getApplicationContext(),
                        data.getDownloadUrl(),
                        data.getDownloadAuthCookie(0).getName() + "=" + data.getDownloadAuthCookie(0).getValue()
                    );

                    if (f != null) {
                        if (FileUtil.installApk(f.getAbsolutePath())) {
                            if (InstalledAppUtil.getAppVersionCode(this, u.getPname()) == u.getNewVersionCode()) {
                                success.add(u);
                                mLogger.log("installApps", "Installed " + u.getPname(), LogMessage.SEVERITY_INFO);
                            } else {
                                failure.add(u);
                                mLogger.log("installApps", "Failed to install " + u.getPname(), LogMessage.SEVERITY_INFO);
                            }
                        } else {
                            failure.add(u);
                            mLogger.log("installApps", "Failed to install " + u.getPname(), LogMessage.SEVERITY_INFO);
                        }

                        f.delete();
                    } else {
                        failure.add(u);
                        mLogger.log("installApps", "Error downloading " + u.getPname(), LogMessage.SEVERITY_INFO);
                    }
                } catch (Exception e) {
                    failure.add(u);
                    mLogger.log("installApps " + u.getPname(), String.valueOf(e), LogMessage.SEVERITY_ERROR);
                }
            }
            doFinalNotification(success, failure);
        } catch (Exception e) {
	        doErrorNotification();
	        mLogger.log("installApps", String.valueOf(e), LogMessage.SEVERITY_ERROR);
        }
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected void onHandleIntent(
		Intent intent
	) {
	    String u = intent.getExtras().getString("updates");
        Update [] updates = new Gson().fromJson(u, Update[].class);
        installApps(updates);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
