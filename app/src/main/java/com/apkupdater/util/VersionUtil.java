package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;

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
        List<Integer> version = new ArrayList<>();

		// First split string with spaces, - and .
		for (String i : full_string.split("( )|(-)|(\\.)")) {
            try {
                int c = Integer.parseInt(
                    // Remove v to properly read versions like v0.0.1
                    i.replace("v", "").replace("V", "").replace("b", "").replace("B", "").replace("u", "").replace("U", "")
                );
                version.add(c);
            } catch (NumberFormatException e) {
                break;
            }
		}

        // If we have at least 2 numbers consider it a version
        if (version.size() >= 2) {
            return version;
        }

		// If that failed, try making a version from all numbers on the string
		version.clear();
		for (String i : full_string.replaceAll("[^0-9]+", " ").trim().split(" ")) {
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

    static public  <T> List<List<T>> batchList(
        List<T> list,
        int batchSize
    ) {
        int listSize = list.size();
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < listSize; i += batchSize) {
            parts.add(new ArrayList<>(list.subList(i, Math.min(listSize, i + batchSize))));
        }
        return parts;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean skipArchitecture(
        List<String> arches
    ) {
		if (arches.isEmpty()) {
			return false;
		}

        String arch = "arm";

        if (Build.CPU_ABI.contains("arm")) {
            arch = "arm";
        } else if (Build.CPU_ABI.contains("mips")) {
            arch = "mips";
        } else if (Build.CPU_ABI.contains("x86")) {
            arch = "x86";
        }

        for (String a : arches) {
            if (a.contains(arch) || a.contains("universal") || a.contains("noarch")) {
                return false;
            }
        }

	    return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public boolean skipMinapi(
        String minApi
    ) {
	    try {
			if (minApi.equalsIgnoreCase("o")) {
				minApi = "26";
			}

            // If minapi is higher than current api, skip this
            if (Integer.valueOf(minApi) > Build.VERSION.SDK_INT) {
                return true;
            }

            return false;
        } catch (Exception e) {
	        return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
