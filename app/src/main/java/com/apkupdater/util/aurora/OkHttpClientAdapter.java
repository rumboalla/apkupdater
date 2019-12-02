/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.apkupdater.util.aurora;

import android.content.Context;

import com.dragons.aurora.playstoreapiv2.AuthException;
import com.dragons.aurora.playstoreapiv2.GooglePlayAPI;
import com.dragons.aurora.playstoreapiv2.GooglePlayException;
import com.dragons.aurora.playstoreapiv2.HttpClientAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpClientAdapter extends HttpClientAdapter {

    private OkHttpClient client;

    public OkHttpClientAdapter(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                });
        //if (Util.isNetworkProxyEnabled(context)) builder.proxy(Util.getNetworkProxy(context));
        client = builder.build();
    }

    @Override
    public byte[] get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(buildUrl(url, params))
                .get();
        return request(requestBuilder, headers);
    }

    @Override
    public byte[] getEx(String url, Map<String, List<String>> params, Map<String, String> headers) throws IOException {
        return request(new Request.Builder().url(buildUrlEx(url, params)).get(), headers);
    }

    @Override
    public byte[] postWithoutBody(String url, Map<String, String> urlParams, Map<String, String> headers) throws IOException {
        return post(buildUrl(url, urlParams), new HashMap<>(), headers);
    }

    @Override
    public byte[] post(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        FormBody.Builder bodyBuilder = new FormBody.Builder();
        if (null != params && !params.isEmpty()) {
            for (String name : params.keySet()) {
                bodyBuilder.add(name, params.get(name));
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(bodyBuilder.build());

        return post(url, requestBuilder, headers);
    }

    @Override
    public byte[] post(String url, byte[] body, Map<String, String> headers) throws IOException {
        if (!headers.containsKey("Content-Type")) {
            headers.put("Content-Type", "application/x-protobuf");
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/x-protobuf"), body));

        return post(url, requestBuilder, headers);
    }

    private byte[] post(String url, Request.Builder requestBuilder, Map<String, String> headers) throws IOException {
        requestBuilder.url(url);

        return request(requestBuilder, headers);
    }


    private byte[] request(Request.Builder requestBuilder, Map<String, String> headers) throws IOException {
        Request request = requestBuilder
                .headers(Headers.of(headers))
                .build();
        Response response = client.newCall(request).execute();

        int code = response.code();
        byte[] content = response.body().bytes();

        if (code == 401 || code == 403) {
            AuthException authException = new AuthException("Auth error", code);
            Map<String, String> authResponse = GooglePlayAPI.parseResponse(new String(content));
            if (authResponse.containsKey("Error") && authResponse.get("Error").equals("NeedsBrowser")) {
                authException.setTwoFactorUrl(authResponse.get("Url"));
            }
            throw authException;
        } else if (code == 404) {
            Map<String, String> authResponse = GooglePlayAPI.parseResponse(new String(content));
            if (authResponse.containsKey("Error") && authResponse.get("Error").equals("UNKNOWN_ERR")) {
                throw new UnknownException("Unknown error occurred", code);
            } else
                throw new AppNotFoundException("App not found", code);
        } else if (code == 429) {
            throw new TooManyRequestsException("Rate-limiting enabled, you are making too many requests", code);
        } else if (code >= 500) {
            throw new GooglePlayException("Server error", code);
        } else if (code >= 400) {
            throw new MalformedRequestException("Malformed Request", code);
        }
        return content;
    }

    public String buildUrl(String url, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (null != params && !params.isEmpty()) {
            for (String name : params.keySet()) {
                urlBuilder.addQueryParameter(name, params.get(name));
            }
        }
        return urlBuilder.build().toString();
    }

    public String buildUrlEx(String url, Map<String, List<String>> params) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (null != params && !params.isEmpty()) {
            for (String name : params.keySet()) {
                for (String value : params.get(name)) {
                    urlBuilder.addQueryParameter(name, value);
                }
            }
        }
        return urlBuilder.build().toString();
    }
}