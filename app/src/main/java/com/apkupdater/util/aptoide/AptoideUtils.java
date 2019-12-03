package com.apkupdater.util.aptoide;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/*
	Horrible code from aptoide-client-v8 (https://github.com/Aptoide/aptoide-client-v8)
 */
public class AptoideUtils {

	public static String getFilters(Activity context) {
		try {
			String filters = "maxSdk="
					+ Build.VERSION.SDK_INT
					+ "&maxScreen="
					+ getScreenSize(Resources.getSystem())
					+ "&maxGles="
					+ ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().getGlEsVersion()
					+ "&myCPU="
					+ getAbis()
					+ "&leanback="
					+ hasLeanback(context)
					+ "&myDensity="
					+ getDensityDpi(context.getWindowManager());
			//+ (getSupportedOpenGlExtensionsManager().equals("") ? ""
			//: "&myGLTex=" + getSupportedOpenGlExtensionsManager());

			return Base64.encodeToString(filters.getBytes(), 0)
					.replace("=", "")
					.replace("/", "*")
					.replace("+", "_")
					.replace("\n", "");
		} catch (Exception e) {
			Log.e("AptoideUtils", "getFilters", e);
			return "";
		}
	}

	public enum Size {
		notfound, small, normal, large, xlarge;

		private static final String TAG = Size.class.getSimpleName();

		public static Size lookup(String screen) {
			try {
				return valueOf(screen);
			} catch (Exception e) {
				return notfound;
			}
		}
	}


	public static int getDensityDpi(WindowManager windowManager) {
		DisplayMetrics metrics = new DisplayMetrics();
		windowManager.getDefaultDisplay()
				.getMetrics(metrics);

		int dpi = metrics.densityDpi;

		if (dpi <= 120) {
			dpi = 120;
		} else if (dpi <= 160) {
			dpi = 160;
		} else if (dpi <= 213) {
			dpi = 213;
		} else if (dpi <= 240) {
			dpi = 240;
		} else if (dpi <= 320) {
			dpi = 320;
		} else if (dpi <= 480) {
			dpi = 480;
		} else {
			dpi = 640;
		}

		return dpi;
	}

	public static String hasLeanback(Context context) {
		if (((UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE)).getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
			return "1";
		} else {
			return "0";
		}
	}

	public static String getAbis() {
		final String[] abis = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Build.SUPPORTED_ABIS
				: new String[] { Build.CPU_ABI, Build.CPU_ABI2 };
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < abis.length; i++) {
			builder.append(abis[i]);
			if (i < abis.length - 1) {
				builder.append(",");
			}
		}
		return builder.toString();
	}

	public static String getScreenSize(Resources resources) {
		return Size.values()[getScreenSizeInt(resources)].name().toLowerCase(Locale.ENGLISH);
	}

	private static int getScreenSizeInt(Resources resources) {
		return resources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
	}

	private static byte[] computeSha1(byte[] bytes) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(bytes, 0, bytes.length);
			return md.digest();
		} catch (NoSuchAlgorithmException e) {
			Log.e("AptoideUtils", "computeSha1", e);
		}

		return new byte[0];
	}

	public static String computeSha1WithColon(byte[] bytes) {
		return convToHexWithColon(computeSha1(bytes)).toUpperCase(Locale.ENGLISH);
	}

	private static String convToHexWithColon(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9)) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);

			if (i < data.length - 1) {
				buf.append(":");
			}
		}
		return buf.toString();
	}

}
