package de.apkgrabber.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAptoide
        extends UpdaterBase {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static final private String BaseUrl = "https://ws75.aptoide.com";
    static final private String Type = "Aptoide";

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterAptoide(
            Context context,
            String pname,
            String cversion
    ) {
        super(context, pname, cversion, Type);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected String getUrl(
            String pname
    ) {
        return BaseUrl + "/api/7/app/get/package_name=" + pname;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected UpdaterStatus parseUrl(
            String url
    ) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode root = mapper.readTree(new URL(url));

            if (root.get("info").get("status").asText().equals("OK")) {
                final JsonNode file = root.get("nodes").get("meta").get("data").get("file");
                final String versionName = file.get("vername").asText();
                final int versionCode = file.get("vercode").asInt();
                final String apkDownloadUrl = file.get("path").asText();

                if (compareVersions(mCurrentVersion, versionName) == -1) {
                    mResultUrl = apkDownloadUrl;
                    mResultVersion = versionName;
                    mResultVersionCode = versionCode;
                    return UpdaterStatus.STATUS_UPDATE_FOUND;
                }
            }
        } catch (Exception e) {
            if(appExists(url)) {
                mError = addCommonInfoToError(e);
                return UpdaterStatus.STATUS_ERROR;
            }
        }

        return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
    }

    private boolean appExists(String url) {
        try {
            URL url2Check = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) url2Check.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
}
