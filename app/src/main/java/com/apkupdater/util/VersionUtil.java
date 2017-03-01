package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageInfo;

import com.apkupdater.R;

import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class VersionUtil {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public String getStringVersionFromString(
		String full_string
	) {
		List<Integer> v = getVersionFromString(full_string);
		String version = "";
		for (Integer i : v) {
			version += i + ".";
		}
		return version.substring(0, version.length() - 1);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public List<Integer> getVersionFromString(
		String full_string
	) {
		// First split string with spaces or -
		String [] space_string = full_string.split("( )|(-)");

		for (String i : space_string) {
			// Try to split with "."
			String [] dot_string = i.split("\\.");
			if (dot_string.length < 2) {
				continue;
			}

			// If its a number add it to version
			List<Integer> version = new ArrayList<>();
			for (String j : dot_string) {
				try {
					int c = Integer.parseInt(j.replace("v", "").replace("V", "").replace("b", "").replace("B", "")); // Remove v to properly read versions like v0.0.1
					version.add(c);
				} catch (NumberFormatException e) {
					break;
				}
			}

			// If we have at least 2 numbers consider it a version
			if (version.size() >= 2) {
				return version;
			}
		}

		// If that failed, try making a version from all numbers on the string
		String [] split = full_string.replaceAll("[^0-9]+", " ").trim().split(" ");
		List<Integer> version = new ArrayList<>();
		for (String i : split) {
			version.add(Integer.valueOf(i));
		}
		if (version.size() > 0) {
			return version;
		}
		
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public int compareVersion(
		List<Integer> l,
		List<Integer> r
	) {
		for (int i = 0; i < (l.size() > r.size() ? r.size() : l.size()); i++) {
			if (l.get(i) < r.get(i)) {
				return -1;
			} else if (l.get(i) > r.get(i)) {
				return 1;
			}
		}

		return 0;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public boolean isExperimental(
		String s
	) {
		return s.toLowerCase().contains("beta") || s.toLowerCase().contains("alpha");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public String getUserAgent(
		Context context
	) {
		String version = "0.0.0";
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (Exception ignored) {

		}

		return context.getString(R.string.app_name) + "-v" + version;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
