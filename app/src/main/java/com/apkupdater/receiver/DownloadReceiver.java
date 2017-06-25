package com.apkupdater.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.apkupdater.event.InstallAppEvent;
import com.apkupdater.event.PackageInstallerEvent;
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
                        mBus.post(new InstallAppEvent(null, id, true));
                    } else {
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mBus.post(new PackageInstallerEvent(install, id));
                    }
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

    private void installWithRoot(
        Context context,
        String uriString
    )
        throws  Exception
    {
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
        } catch (Exception e) {
            mLog.log("installWithRoot", String.valueOf(e), LogMessage.SEVERITY_ERROR);
            throw e;
        } finally {
            if (f != null) {
                f.delete();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////