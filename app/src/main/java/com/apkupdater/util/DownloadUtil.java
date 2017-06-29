package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.UUID;

import static android.content.Context.DOWNLOAD_SERVICE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class DownloadUtil
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public void LaunchBrowser(
        Context context,
        String url
    ) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public long downloadFile(
        Context context,
        String url,
        String cookie,
        String name
    ) throws Exception
    {
        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if (Build.VERSION.SDK_INT > 10) {
            //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setTitle(name);
        request.addRequestHeader("Cookie", cookie);
        File dir = context.getExternalCacheDir();
        if (dir == null) {
            dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) {
                throw new Exception("Can't get external directory.");
            }
        }
        request.setDestinationUri(Uri.fromFile(new File(dir, UUID.randomUUID().toString())));
        return dm.enqueue(request);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public void deleteDownloadedFiles(
        @NonNull final Context context
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Query query = new DownloadManager.Query();

                    Cursor cursor = manager.query(query);

                    if (cursor == null) {
                        return;
                    }

                    while (cursor.moveToNext()) {
                        int id = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                        manager.remove(id);
                    }

                    cursor.close();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public void deleteDownloadedFile(
        @NonNull final Context context,
        final long id
    ) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    DownloadManager.Query query = new DownloadManager.Query();

                    Cursor cursor = manager.query(query);

                    if (cursor == null) {
                        return;
                    }

                    while (cursor.moveToNext()) {
                        if(cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID)) == id) {
                            manager.remove(id);
                        }
                    }

                    cursor.close();
                } catch (Exception ignored) {}
            }
        }).start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////