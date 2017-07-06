package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.util.Pair;

import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.yalp.NativeDeviceInfoProvider;
import com.apkupdater.util.yalp.OkHttpClientAdapter;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class GooglePlayUtil
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private static final String [] GSFID = {
		"326b94da1d625a12",
		"3e2d9529d52d8d6c",
		"3321ca20a1b56509",
		"35b1905948d34f68",
		"3205280df61a4644"
	};

	private static final String [] TOKEN = {
		"2ATSi286IOTP_2vbf7m6uNSmqzdsWiWcRmqmNwOYrrZs0jnTyNwyB4wn204qHHldlle_Qg.",
		"2AR9Nn0xfO-gHRigTZVBTrrwNPtebofzYssA9q2SH_c1q7W8S8rDkfpUg0KO--3ArHbuIQ.",
		"2AQgOEr9h_Wb8lp4isUymIJYJrz4TDHZ-zrZyHlcr0hmTbuOD4pVd1TrpO-iNhf9ntiuSw.",
		"2AS4jUFSPpP-UHmruqWnOyKVdNaE9GsYa1NV9EZJHgkWvfSVa-0WwepqTczdBoPM3HL3CQ.",
		"1gTCN_ouDuqbAVg7UViJL5tJ00m7Pc061nEWsf8SLQdE0bCIkhrqp6rUSikdxEaJA9sGCQ."
	};

	private static int i = getCounter();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public GooglePlayAPI getApi(
		Context context
	) {
		GooglePlayAPI api = null;
		int c = 0;

		while (api == null && c < 10) {
			try {


				com.github.yeriomin.playstoreapi.PlayStoreApiBuilder builder = new com.github.yeriomin.playstoreapi.PlayStoreApiBuilder()
					.setHttpClient(new OkHttpClientAdapter())
					.setDeviceInfoProvider(getNativeProvider(context))
					.setLocale(Locale.getDefault())
					.setEmail(null)
					.setPassword(null);

                UpdaterOptions options = new UpdaterOptions(context);
				if (options.useOwnPlayAccount()) {
                    builder.setGsfId(options.getOwnGsfId()).setToken(options.getOwnToken());
                } else {
					builder.setGsfId(GSFID[i]).setToken(TOKEN[i]);
                }

				api = builder.build();
				api.uploadDeviceConfig();
			} catch (Exception e) {
				api = null;
				i = getCounter();
			} finally {
				c++;
			}
		}

		return api;
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static DeviceInfoProvider getNativeProvider(
	    Context context
    ) {
        DeviceInfoProvider deviceInfoProvider = new NativeDeviceInfoProvider();
        ((NativeDeviceInfoProvider) deviceInfoProvider).setContext(context);
        ((NativeDeviceInfoProvider) deviceInfoProvider).setLocaleString(Locale.getDefault().toString());
        return deviceInfoProvider;
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

	public static Pair<String, String> getIdTokenPairFromEmailPassword(
	    Context context,
        String email,
        String password
    ) throws Exception {

        com.github.yeriomin.playstoreapi.PlayStoreApiBuilder builder = new com.github.yeriomin.playstoreapi.PlayStoreApiBuilder()
            .setHttpClient(new OkHttpClientAdapter())
            .setDeviceInfoProvider(getNativeProvider(context))
            .setLocale(Locale.getDefault())
            .setEmail(email)
            .setPassword(password);

        GooglePlayAPI api = builder.build();
	    return new Pair<>(api.getGsfId(), api.getToken());
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static int getCounter(
    ) {
        return new Random().nextInt(GSFID.length);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////