package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import static android.content.Context.DOWNLOAD_SERVICE;

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

    static public void downloadFile(
        Context context,
        String url,
        String cookie,
        String name
    ) {
        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        if (Build.VERSION.SDK_INT > 10) {
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setTitle(name);
        request.addRequestHeader("Cookie", cookie);
        dm.enqueue(request);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public void deleteDownloadedFiles(
        Context context
    ) {
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////