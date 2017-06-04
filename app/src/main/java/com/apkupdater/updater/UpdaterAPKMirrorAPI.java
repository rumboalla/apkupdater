package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.model.APKMirror.AppExistsRequest;
import com.apkupdater.model.APKMirror.AppExistsResponse;
import com.apkupdater.model.APKMirror.AppExistsResponseApk;
import com.apkupdater.model.APKMirror.AppExistsResponseData;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.Update;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.VersionUtil;
import com.google.gson.Gson;

import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
    private static final String DownloadUrl = "https://www.apkmirror.com";
    private static final String AppExists = "app_exists/";
    private static final String User = "api-apkupdater";
    private static final String Token = "rm5rcfruUjKy04sMpyMPJXW8";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private List<InstalledApp> mApps;
    private Context mContext;
    private String mError;
    private UpdaterStatus mResultCode = UpdaterStatus.STATUS_UPDATE_FOUND;
    private List<Update> mUpdates = new ArrayList<>();
    private GenericCallback<Update> mUpdateCallback;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterAPKMirrorAPI(
        Context context,
        List<InstalledApp> apps,
        GenericCallback<Update> updateCallback
    ) {
        // Store vars
        mApps = apps;
        mContext = context;
        mUpdateCallback = updateCallback;

        // Create the OkHttp client
        OkHttpClient client = getOkHttpClient();
        if (client == null) {
            mError = "Unable to get OkHttpError";
            mResultCode = UpdaterStatus.STATUS_ERROR;
            return;
        }

        // Build the json object for the request
        List<String> pnames = new ArrayList<>();
        for (InstalledApp app : apps) {
            pnames.add(app.getPname());
        }

        AppExistsRequest json = new AppExistsRequest(
            pnames,
            new UpdaterOptions(context).skipExperimental() ? AppExistsRequest.excludeExperimental() : null
        );

        // Build the OkHttp request
        RequestBody body = RequestBody.create(JSON, new Gson().toJson(json));
        final Request request = new Request.Builder()
            .url(BaseUrl + AppExists)
            .post(body)
            .header("Authorization", Credentials.basic(User, Token))
            .build();

        // Perform request
        try {
            Response r = client.newCall(request).execute();
            parseResponse(r.body().string());
        } catch (Exception e) {
            mError = "Request failure: " + String.valueOf(e);
            mResultCode = UpdaterStatus.STATUS_ERROR;
            return;
        }
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
                mError = "Request not successful: " + r.getStatus();
                mResultCode = UpdaterStatus.STATUS_ERROR;
                return;
            }

            UpdaterOptions options = new UpdaterOptions(mContext);
            boolean skipExperimental = options.skipExperimental();
            boolean skipArchitecture = options.skipArchitecture();
            boolean skipMinapi = options.skipMinapi();

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
                        // Check if app is beta and if we should skip it
                        boolean isBeta = VersionUtil.isExperimental(data.getRelease().getVersion());
                        if (isBeta && skipExperimental) {
                            continue;
                        }

                        if (skipArchitecture && apk.getArches() != null && VersionUtil.skipArchitecture(apk.getArches())) {
                            continue;
                        }

                        if (skipMinapi && apk.getMinapi() != null && VersionUtil.skipMinapi(apk.getMinapi())) {
                            continue;
                        }

                        // Add the update
                        Update u = new Update(
                            app,
                            DownloadUrl + data.getRelease().getLink(),
                            data.getRelease().getVersion(),
                            isBeta
                        );

                        mUpdates.add(u);
                        mUpdateCallback.onResult(u);
                        break;
                    }

                    mUpdateCallback.onResult(null);
                }
            }

        } catch (Exception e) {
            mError = "Parse response failed: " + String.valueOf(e);
            mResultCode = UpdaterStatus.STATUS_ERROR;
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

    public Throwable getResultError(
    ) {
        return new Throwable(mError);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterStatus getResultStatus(
    ) {
        return mResultCode;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<Update> getUpdates(
    ) {
        return mUpdates;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
