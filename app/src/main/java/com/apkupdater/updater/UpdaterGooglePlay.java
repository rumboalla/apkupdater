package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.apkupdater.util.VersionUtil;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;

import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterGooglePlay
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String Type = "GooglePlay";
    static final private String DownloadUrl = "https://play.google.com/store/apps/details?id=";
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

    private GooglePlayAPI initApi() {
	    try {
	        Thread.sleep(100);
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
            // TODO: Do this crap properly
            mLock.lock();
            while (mApi == null) {
                mApi = initApi();
            }
            mLock.unlock();

            DetailsResponse response = mApi.details(pname);
            DocV2 details = response.getDocV2();
            String v = details.getDetails().getAppDetails().getVersionString();
            int c = details.getDetails().getAppDetails().getVersionCode();

            if (c > Integer.valueOf(mCurrentVersion)) {
                mResultUrl = DownloadUrl + pname;
                mResultVersion = VersionUtil.getStringVersionFromString(v);
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