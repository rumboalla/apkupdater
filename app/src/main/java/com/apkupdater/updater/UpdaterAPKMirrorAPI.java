package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.model.APKMirror.AppExistsRequest;
import com.apkupdater.model.APKMirror.AppExistsResponse;
import com.apkupdater.model.APKMirror.AppExistsResponseApk;
import com.apkupdater.model.APKMirror.AppExistsResponseData;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAPKMirrorAPI
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String BaseUrl = "https://www.apkmirror.com/wp-json/apkm/v1/";
    private static final String AppExists = "app_exists/";
    private static final String User = "api-apkupdater";
    private static final String Token = "rm5rcfruUjKy04sMpyMPJXW8";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private List<InstalledApp> mApps;
    private Context mContext;
    private MyBus mBus;
    private LogUtil mLog;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterAPKMirrorAPI(
        Context context,
        MyBus bus,
        LogUtil log,
        List<InstalledApp> apps
    ) {
        // Store vars
        mApps = apps;
        mContext = context;
        mBus = bus;
        mLog = log;

        // Create the OkHttp client
        OkHttpClient client = getOkHttpClient();
        if (client == null) {
            mLog.log("UpdaterAPKMirrorAPI", "Unable to get OkHttpClient.", LogMessage.SEVERITY_ERROR);
            return;
        }

        // Build the json object for the request
        List<String> pnames = new ArrayList<>();
        for (InstalledApp app : apps) {
            pnames.add(app.getPname());
        }
        AppExistsRequest json = new AppExistsRequest(pnames);

        // Build the OkHttp request
        RequestBody body = RequestBody.create(JSON, new Gson().toJson(json));
        final Request request = new Request.Builder()
            .url(BaseUrl + AppExists)
            .post(body)
            .header("Authorization", Credentials.basic(User, Token))
            .build();

        // Perform request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mLog.log("UpdaterAPKMirrorAPI", "Request failure: " + String.valueOf(e), LogMessage.SEVERITY_ERROR);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                parseResponse(response.body().string());
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void parseResponse(
        String body
    ) {
        try {
            // Convert response json to object
            AppExistsResponse r = new Gson().fromJson(body, AppExistsResponse.class);

            // Check if request was successful (Code 200)
            if (r.getStatus() != 200) {
                mLog.log("UpdaterAPKMirrorAPI", "Request not successful: " + r.getStatus(), LogMessage.SEVERITY_ERROR);
                return;
            }

            // Check the response data
            for (AppExistsResponseData data : r.getData()) {
                // If no apk, check next data
                if (data.getApks() == null) {
                    continue;
                }

                // Check all apks
                for (AppExistsResponseApk apk : data.getApks()) {
                    InstalledApp app = getInstalledApp(data.getPname());
                    if (app != null && Integer.valueOf(apk.getVersionCode()) > app.getVersionCode()) {
                        mLog.log("Update Found", apk.getLink(), LogMessage.SEVERITY_INFO);
                        // TODO: Find a way to properly return updates
                        break;
                    }
                    // TODO: (NEW FEATURE) Filter architecture and API level
                }
            }

        } catch (Exception e) {
            // TODO: Log error
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private InstalledApp getInstalledApp(
        String pname
    ) {
        for (InstalledApp app : mApps) {
            if (app.getPname().equals(pname)) {
                return app;
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private OkHttpClient getOkHttpClient(
    ) {
        try {
            final TrustManager[] trust = new TrustManager[] {
                new X509TrustManager() {
                    @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                    @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                    @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
            };

            SSLContext context = SSLContext.getInstance("SSL");
            context.init(null, trust, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                .sslSocketFactory(context.getSocketFactory(), (X509TrustManager)trust[0])
                .build();

        } catch (Exception e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
