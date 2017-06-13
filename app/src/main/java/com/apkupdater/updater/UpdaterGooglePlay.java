package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.Update;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.PixelConversion;
import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.apkupdater.util.VersionUtil;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.BulkDetailsEntry;
import com.github.yeriomin.playstoreapi.BulkDetailsResponse;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.github.yeriomin.playstoreapi.PurchaseStatusResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.MediaType;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterGooglePlay
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static GooglePlayAPI mApi = null;
    private static ReentrantLock mLock = new ReentrantLock();

    private List<InstalledApp> mApps;
    private Context mContext;
    private String mError;
    private UpdaterStatus mResultCode = UpdaterStatus.STATUS_UPDATE_FOUND;
    private List<Update> mUpdates = new ArrayList<>();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UpdaterGooglePlay(
        Context context,
        List<InstalledApp> apps
    ) {
        try {
            // Store vars
            mApps = apps;
            mContext = context;
            mApi = initApi();

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
            }

            for (BulkDetailsEntry entry : response.getEntryList()) {
                if (!entry.hasDoc()) {
                    continue;
                }

                DocV2 details = entry.getDoc();
                int versionCode = details.getDetails().getAppDetails().getVersionCode();
                String pname = details.getDetails().getAppDetails().getPackageName();
                InstalledApp app = getInstalledApp(pname);

                if (app == null) {
                    continue;
                }

                if (versionCode > app.getVersionCode()) {
                    if (details.getOfferCount() == 0) {
                        continue;
                    }

                    PurchaseStatusResponse r;
                    try {
                        r = mApi.purchase(pname, versionCode, details.getOffer(0).getOfferType()).getPurchaseStatusResponse();
                        if (r.getStatus() != 1) {
                            continue;
                        }
                    } catch (GooglePlayException ex) {
                        // Paid app most likely
                        continue;
                    }

                    AndroidAppDeliveryData d = r.getAppDeliveryData();
                    if (d.getDownloadAuthCookieCount() == 0) {
                        continue;
                    }

                    Update u = new Update(
                        app,
                        d.getDownloadUrl(),
                        details.getDetails().getAppDetails().hasVersionString() ? details.getDetails().getAppDetails().getVersionString() : "?",
                        false,
                        d.getDownloadAuthCookie(0).getName() + "=" + d.getDownloadAuthCookie(0).getValue(),
                        versionCode
                    );
                    mUpdates.add(u);
                }
            }

        } catch (Exception e) {
            mError = String.valueOf(e);
            mResultCode = UpdaterStatus.STATUS_ERROR;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private GooglePlayAPI buildApi() {
	    try {
            DeviceInfoProvider deviceInfoProvider = new NativeDeviceInfoProvider();
            ((NativeDeviceInfoProvider) deviceInfoProvider).setContext(mContext);
            ((NativeDeviceInfoProvider) deviceInfoProvider).setLocaleString(Locale.getDefault().toString());

            com.github.yeriomin.playstoreapi.PlayStoreApiBuilder builder = new com.github.yeriomin.playstoreapi.PlayStoreApiBuilder()
                .setHttpClient(new OkHttpClientAdapter())
                .setDeviceInfoProvider(deviceInfoProvider)
                .setLocale(Locale.getDefault())
                .setEmail(null)
                .setPassword(null)
                .setGsfId("332840dd1f6fbcb7")
                .setToken("xwTCN6FyGkcpTew5FcY5NqOchMjh5bV5u172U2hJELXLGcGXCpCFqJFlBpjjOWjeG3jDng.");

            GooglePlayAPI api = builder.build();
            api.uploadDeviceConfig();
            return api;
        } catch (Exception e) {
	        return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private GooglePlayAPI initApi(
    ) {
        mLock.lock();
        int c = 0;
        while (mApi == null && c < 10) {
			mApi = buildApi();
            c++;
        }
        mLock.unlock();
        return mApi;
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