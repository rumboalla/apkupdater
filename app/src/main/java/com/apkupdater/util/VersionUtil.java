package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class VersionUtil {

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
					int c = Integer.parseInt(j.replace("v", "")); // Remove v to properly read versions like v0.0.1
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
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////