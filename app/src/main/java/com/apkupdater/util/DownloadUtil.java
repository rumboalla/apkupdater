package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
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

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////