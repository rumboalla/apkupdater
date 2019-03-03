package de.apkgrabber.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class APKMirrorParser {
    public static String parseDownloadUrlFromWebsite(String url) {
        String downloadUrl = "";
        try {
            OkHttpClient client = new OkHttpClient();
            Request r = new Request.Builder().url(url).build();
            String response = client.newCall(r).execute().body().string();

            String baseUrl = "/wp-content/themes/APKMirror/";
            Pattern pattern = Pattern.compile(baseUrl + ".*here");
            Matcher match = pattern.matcher(response);

            if(match.find()) {
                downloadUrl = response.substring(match.start(), match.end() - 6);
            }
        } catch (Exception e) {
        }
        return downloadUrl;
    }
}
