package com.apkupdater.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.apkupdater.event.InstallAppEvent;
import com.apkupdater.event.PackageInstallerEvent;
import com.apkupdater.model.Constants;
import com.apkupdater.model.LogMessage;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.FileUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

import java.io.File;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EReceiver
public class DownloadReceiver
    extends BroadcastReceiver
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Bean
    LogUtil mLog;

    @Bean
    MyBus mBus;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(
        final Context context,
        final Intent intent
    ) {
        // Launch install
        if (intent.getAction().equals(Constants.DownloadAction)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    openDownloadedFileDirect(context, intent);
                }
            }).start();
        } else if (intent.getAction().equals(Constants.DownloadManagerAction)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    openDownloadedFile(context, intent);
                }
            }).start();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean openDownloadedFile(
        final Context context,
        final Intent intent
    ) {
        long id = -1;
        try {
            id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = manager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                if (status == DownloadManager.STATUS_SUCCESSFUL && uriString != null) {
                    install(context, uriString, id);
                } else {
                    throw new Exception("Error downloading.");
                }
            } else {
				throw new Exception("Download cancelled.");
			}
            cursor.close();
            return true;
        } catch (Exception e) {
            mLog.log("openDownloadFile", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            mBus.post(new InstallAppEvent(null, id, false));
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean openDownloadedFileDirect(
        final Context context,
        final Intent intent
    ) {
        long id = -1;
        try {
            id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            boolean status = intent.getBooleanExtra(DownloadManager.COLUMN_STATUS, false);
            String uriString = intent.getStringExtra(DownloadManager.COLUMN_LOCAL_URI);
            if (status) {
                install(context, uriString, id);
            } else {
                throw new Exception("Error downloading.");
            }

            return true;
        } catch (Exception e) {
            mLog.log("openDownloadFile", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            mBus.post(new InstallAppEvent(null, id, false));
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void install(
        Context context,
        String uriString,
        long id
    )
        throws  Exception
    {
        UpdaterOptions options = new UpdaterOptions(context);

        if (options.useRootInstall()) {
            installWithRoot(uriString);
            mBus.post(new InstallAppEvent(null, id, true));
        } else {
            Uri u = Uri.parse(uriString);
            Intent install;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                install.setDataAndType(
                    FileProvider.getUriForFile(context, Constants.FileProvider, new File(u.getPath())),
                    Constants.ApkMime
                );
                install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(u, Constants.ApkMime);
            }

            mBus.post(new PackageInstallerEvent(install, id));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void installWithRoot(
        String uriString
    )
        throws  Exception
    {
        try {
            File f = new File(Uri.parse(uriString).getPath());
            FileUtil.installApk(f.getAbsolutePath());
        } catch (Exception e) {
            mLog.log("installWithRoot", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            throw e;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////