package com.apkupdater.receiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import org.androidannotations.annotations.EReceiver;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EReceiver
public class DownloadReceiver
    extends BroadcastReceiver
{

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onReceive(
        Context context,
        Intent intent
    ) {
        // Check if it's a download
        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
        if (id == -1) {
            return;
        }

        // Launch install
        openDownloadedFile(context, id);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void openDownloadedFile(
        final Context context,
        final long id
    ) {
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor = manager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            //String mime = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
            if (status == DownloadManager.STATUS_SUCCESSFUL && uri != null) {
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(install);
            }
        }
        cursor.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////