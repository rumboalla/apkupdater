package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.util.Log;

import com.apkupdater.model.APKMirror.AppExistsRequest;
import com.apkupdater.model.InstalledApp;
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterAPKMirrorAPI(
        Context context,
        List<InstalledApp> apps
    ) {
        // Create the OkHttp client
        OkHttpClient client = getOkHttpClient();
        if (client == null) {
            // TODO: Send error
            return;
        }

        // Build the json object for the request
        List<String> pnames = new ArrayList<>();
        for (InstalledApp app : apps) {
            pnames.add(app.getPname());
        }
        AppExistsRequest json = new AppExistsRequest(pnames);

        // Build the OkHttp request
        String test = new Gson().toJson(json);
        RequestBody body = RequestBody.create(JSON, new Gson().toJson(json));
        final Request request = new Request.Builder().url(BaseUrl + AppExists).post(body).header("Authorization", Credentials.basic(User, Token)).build();

        // Perform request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Response", String.valueOf(e));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("Response", response.body().string());
            }
        });
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
