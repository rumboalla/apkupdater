package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.Update;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.github.yeriomin.playstoreapi.BulkDetailsEntry;
import com.github.yeriomin.playstoreapi.BulkDetailsResponse;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterGooglePlay
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static GooglePlayAPI mApi = null;

    private List<InstalledApp> mApps;
    private Context mContext;
    private String mError;
    private UpdaterStatus mResultCode = UpdaterStatus.STATUS_UPDATE_FOUND;
    private List<Update> mUpdates = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterGooglePlay(
        Context context,
        List<InstalledApp> apps,
        ExecutorService executor,
        final GenericCallback<Update> callback
    ) {
        try {
            // Store vars
            mApps = apps;
            mContext = context;
            mApi = getGooglePlayApi(mContext);

            if (mApi == null) {
                mError = "Unable to get GooglePlayApi";
                mResultCode = UpdaterStatus.STATUS_ERROR;
                return;
            }

            List<String> pnames = new ArrayList<>();
            for (InstalledApp app : apps) {
                pnames.add(app.getPname());
            }

            BulkDetailsResponse response = mApi.bulkDetails(pnames);

            if (response == null || response.getEntryList() == null) {
                mError = "Response is null";
                mResultCode = UpdaterStatus.STATUS_ERROR;
                return;
            }

            for (BulkDetailsEntry entry : response.getEntryList()) {
                if (!entry.hasDoc()) {
                    callback.onResult(null);
                    continue;
                }

                final DocV2 details = entry.getDoc();
                final int versionCode = details.getDetails().getAppDetails().getVersionCode();
                final String pname = details.getDetails().getAppDetails().getPackageName();
                final InstalledApp app = getInstalledApp(pname);

                if (app == null) {
                    callback.onResult(null);
                    continue;
                }

                if (versionCode > app.getVersionCode()) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Update u = new Update(
                                    app,
                                    "",
                                    details.getDetails().getAppDetails().hasVersionString() ? details.getDetails().getAppDetails().getVersionString() : "?",
                                    false,
                                    "Cookie",
                                    versionCode
                                );

                                mUpdates.add(u);
                                callback.onResult(u);
                            } catch (Exception ex) {
                                callback.onResult(null);
                            }
                        }
                    });
                }
            }

            executor.shutdown();
            while (!executor.isTerminated()) {
                Thread.sleep(1);
            }

        } catch (Exception e) {
            mError = String.valueOf(e);
            mResultCode = UpdaterStatus.STATUS_ERROR;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public GooglePlayAPI getGooglePlayApi(
        Context context
    ) {
        GooglePlayAPI api = null;
        int c = 0;

        while (api == null && c < 10) {
            try {
                DeviceInfoProvider deviceInfoProvider = new NativeDeviceInfoProvider();
                ((NativeDeviceInfoProvider) deviceInfoProvider).setContext(context);
                ((NativeDeviceInfoProvider) deviceInfoProvider).setLocaleString(Locale.getDefault().toString());

                com.github.yeriomin.playstoreapi.PlayStoreApiBuilder builder = new com.github.yeriomin.playstoreapi.PlayStoreApiBuilder()
                    .setHttpClient(new OkHttpClientAdapter())
                    .setDeviceInfoProvider(deviceInfoProvider)
                    .setLocale(Locale.getDefault())
                    .setEmail(null)
                    .setPassword(null)
                    .setGsfId("332840dd1f6fbcb7")
                    .setToken("xwTCN6FyGkcpTew5FcY5NqOchMjh5bV5u172U2hJELXLGcGXCpCFqJFlBpjjOWjeG3jDng.");

                api = builder.build();
                api.uploadDeviceConfig();
            } catch (Exception e) {
                api = null;
            } finally {
                c++;
            }
        }

        return api;
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

    public Throwable getResultError(
    ) {
        return new Throwable(mError + " | Source: GooglePlay");
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