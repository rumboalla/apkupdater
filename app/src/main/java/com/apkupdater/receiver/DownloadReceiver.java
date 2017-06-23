package com.apkupdater.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;

import com.apkupdater.event.InstallAppEvent;
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
        Intent intent
    ) {
        // Check if it's a download
        final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (id == -1) {
            return;
        }

        // Launch install
        new Thread(new Runnable() {
            @Override
            public void run() {
                openDownloadedFile(context, id);
            }
        }).start();

        // Post the event
        //mBus.post(new DownloadCompleteEvent(b, id));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean openDownloadedFile(
        final Context context,
        final long id
    ) {
        try {
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(id);
            Cursor cursor = manager.query(query);
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                if (status == DownloadManager.STATUS_SUCCESSFUL && uri != null) {
                    UpdaterOptions options = new UpdaterOptions(context);

                    if (options.useRootInstall()) {
                        installWithRoot(context, uri);
                    } else {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            context.startActivity(install);
                        } catch (ActivityNotFoundException e) {
                            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(install);
                        }
                    }
                } else {
                    throw new Exception("Error downloading.");
                }
            }
            cursor.close();
            return true;
        } catch (Exception e) {
            mLog.log("openDownloadFile", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void installWithRoot(
        Context context,
        String uriString
    ) {
        File f = null;
        try {
            f = FileUtil.inputStreamToCacheFile(
                context,
                context.getContentResolver().openInputStream(Uri.parse(uriString))
            );

            if (f == null || !f.exists()) {
                throw new Exception("Unable to copy file before install.");
            }

            FileUtil.installApk(f.getAbsolutePath());

            mBus.post(new InstallAppEvent(true));
        } catch (Exception e) {
            mLog.log("installWithRoot", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            mBus.post(new InstallAppEvent(false));
        } finally {
            if (f != null) {
                f.delete();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////