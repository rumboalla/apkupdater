package com.apkupdater.service;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.IntentService;
import android.content.Intent;

import com.apkupdater.model.AppState;
import com.apkupdater.model.DownloadInfo;
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
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EService
public class AutomaticInstallerService
	extends IntentService
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

	public AutomaticInstallerService(
	) {
		super(AutomaticInstallerService.class.getSimpleName());
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void installApps(
	    Update [] updates
    ) {
	    try {
            for (Update u : updates) {
                try {
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
                            mLogger.log("installApps", "Installed " + u.getPname(), LogMessage.SEVERITY_INFO);
                        } else {
                            mLogger.log("installApps", "Installed " + u.getPname(), LogMessage.SEVERITY_INFO);
                        }
                        f.delete();
                    } else {
                        mLogger.log("installApps", "Error downloading " + u.getPname(), LogMessage.SEVERITY_INFO);
                    }
                } catch (Exception e) {
                    mLogger.log("installApps " + u.getPname(), String.valueOf(e), LogMessage.SEVERITY_ERROR);
                }
            }
        } catch (Exception e) {
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
