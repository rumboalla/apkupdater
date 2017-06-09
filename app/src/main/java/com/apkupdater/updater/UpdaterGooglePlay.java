package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.apkupdater.util.VersionUtil;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.github.yeriomin.playstoreapi.PurchaseStatusResponse;

import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterGooglePlay
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String Type = "GooglePlay";
	private static GooglePlayAPI mApi = null;
    private static ReentrantLock mLock = new ReentrantLock();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterGooglePlay(
		Context context,
		String pname,
	    String cversion
	) {
		super(context, pname, cversion, Type);
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

	@Override
	protected String getUrl(
		String pname
	) {
		return pname;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String pname
	) {
        try {
            GooglePlayAPI api = initApi();
            if (api == null) {
                mError = new Throwable("Unable to get GooglePlayApi.");
                return UpdaterStatus.STATUS_ERROR;
            }

            DetailsResponse response = api.details(pname);
            DocV2 details = response.getDocV2();

            String v = details.getDetails().getAppDetails().getVersionString();
            int versionCode = details.getDetails().getAppDetails().getVersionCode();

            if (versionCode > Integer.valueOf(mCurrentVersion)) {
                if (details.getOfferCount() == 0) {
                    mError = new Throwable("No offers found.");
                    return UpdaterStatus.STATUS_ERROR;
                }

                PurchaseStatusResponse r = api.purchase(pname, versionCode, details.getOffer(0).getOfferType()).getPurchaseStatusResponse();
                if (r.getStatus() != 1) {
                    mError = new Throwable("Error getting app. App could be paid.");
                    return UpdaterStatus.STATUS_ERROR;
                }

                AndroidAppDeliveryData d = r.getAppDeliveryData();
                if (d.getDownloadAuthCookieCount() == 0) {
                    mError = new Throwable("Unable to get download cookie.");
                    return UpdaterStatus.STATUS_ERROR;
                }

                mResultUrl = d.getDownloadUrl();
                mResultVersion = VersionUtil.getStringVersionFromString(v);
                mResultCookie = d.getDownloadAuthCookie(0).getName() + "=" + d.getDownloadAuthCookie(0).getValue();
                mResultVersionCode = versionCode;
                return UpdaterStatus.STATUS_UPDATE_FOUND;
            }

            return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
        } catch (GooglePlayException ex) {
            if (ex.getCode() == 404) {
                return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
            }

            mError = addCommonInfoToError(ex);
            return UpdaterStatus.STATUS_ERROR;
        } catch (Exception e) {
            mError = addCommonInfoToError(e);
            return UpdaterStatus.STATUS_ERROR;
        }

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////