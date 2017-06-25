package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;

import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;

import java.io.IOException;
import java.util.Locale;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class GooglePlayUtil {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public GooglePlayAPI getApi(
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
					.setGsfId("3205280df61a4644")
					.setToken("1gTCN_ouDuqbAVg7UViJL5tJ00m7Pc061nEWsf8SLQdE0bCIkhrqp6rUSikdxEaJA9sGCQ.");

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

	public static AndroidAppDeliveryData getAppDeliveryData(
		GooglePlayAPI api,
		String pname
	) throws IOException {
		DocV2 d = api.details(pname).getDocV2();
		return api.purchase(
			d.getDetails().getAppDetails().getPackageName(),
			d.getDetails().getAppDetails().getVersionCode(),
			d.getOffer(0).getOfferType()
		).getPurchaseStatusResponse().getAppDeliveryData();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////