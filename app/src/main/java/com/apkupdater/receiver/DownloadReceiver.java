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
                String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                if (status == DownloadManager.STATUS_SUCCESSFUL && uriString != null) {
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
                                FileProvider.getUriForFile(context, "com.apkupdater.fileprovider", new File(u.getPath())),
                                "application/vnd.android.package-archive"
                            );
                            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        } else {
                            install = new Intent(Intent.ACTION_VIEW);
                            install.setDataAndType(u, "application/vnd.android.package-archive");
                        }

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