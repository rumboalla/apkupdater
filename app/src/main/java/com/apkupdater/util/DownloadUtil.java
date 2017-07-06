package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.apkupdater.model.Constants;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.DOWNLOAD_SERVICE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class DownloadUtil
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static private long DownloadId = 10000;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public void launchBrowser(
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
    ) throws IOException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return downloadFileDirect(context, url, cookie, name);
        } else {
            return downloadFileDM(context, url, cookie, name);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public long downloadFileDM(
        Context context,
        String url,
        String cookie,
        String name
    ) throws IOException
    {
        DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(name);
        request.addRequestHeader("Cookie", cookie);
        request.setDestinationUri(Uri.fromFile(FileUtil.getRandomFile(context)));
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

    static public long downloadFileDirect(
        @NonNull final Context context,
        @NonNull String url,
        @Nullable String cookie,
        String name
    ) {
        OkHttpClient client = new OkHttpClient();
        Request r = new Request.Builder().url(url).addHeader("Cookie", cookie).build();
        final long id = DownloadId++;
        final Intent i = new Intent(Constants.DownloadAction).putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, id);

        client.newCall(r).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                context.sendBroadcast(i.putExtra(DownloadManager.COLUMN_STATUS, false));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Save file
                File f = FileUtil.getRandomFile(context);
                if(FileUtil.inputStreamToFile(response.body().byteStream(), f)) {
                    i.putExtra(DownloadManager.COLUMN_STATUS, true);
                    i.putExtra(DownloadManager.COLUMN_LOCAL_URI, Uri.fromFile(f).toString());
                    context.sendBroadcast(i);
                } else {
                    context.sendBroadcast(i.putExtra(DownloadManager.COLUMN_STATUS, false));
                }
            }
        });

        return id;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public File downloadFile(
        @NonNull final Context context,
        @NonNull String url,
        @Nullable String cookie
    ) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request r = new Request.Builder().url(url).addHeader("Cookie", cookie).build();
            Response response = client.newCall(r).execute();
            File f = FileUtil.getRandomFile(context);
            boolean b = FileUtil.inputStreamToFile(response.body().byteStream(), f);
            return b ? f : null;
        } catch (Exception e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////