/*
 * Aurora Store
 * Copyright (C) 2019, Rahul Kumar Patel <whyorean@gmail.com>
 *
 * Yalp Store
 * Copyright (C) 2018 Sergey Yeriomin <yeriomin@gmail.com>
 *
 * Aurora Store is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Aurora Store is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora Store.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.apkupdater.util.aurora;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class NativeGsfVersionProvider {

    static private final String GOOGLE_SERVICES_PACKAGE_ID = "com.google.android.gms";
    static private final String GOOGLE_VENDING_PACKAGE_ID = "com.android.vending";

    static private final int GOOGLE_SERVICES_VERSION_CODE = 16089037;
    static private final int GOOGLE_VENDING_VERSION_CODE = 81431900;
    static private final String GOOGLE_VENDING_VERSION_STRING = "14.3.19-all [0] [PR] 241809067";

    private int gsfVersionCode = 0;
    private int vendingVersionCode = 0;
    private String vendingVersionString = "";

    public NativeGsfVersionProvider(Context context) {
        try {
            gsfVersionCode = context.getPackageManager().getPackageInfo(GOOGLE_SERVICES_PACKAGE_ID, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // com.google.android.gms not found
        }
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(GOOGLE_VENDING_PACKAGE_ID, 0);
            vendingVersionCode = packageInfo.versionCode;
            vendingVersionString = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // com.android.vending not found
        }
    }

    public int getGsfVersionCode(boolean defaultIfNotFound) {
        return defaultIfNotFound && gsfVersionCode < GOOGLE_SERVICES_VERSION_CODE
                ? GOOGLE_SERVICES_VERSION_CODE
                : gsfVersionCode;
    }

    public int getVendingVersionCode(boolean defaultIfNotFound) {
        return defaultIfNotFound && vendingVersionCode < GOOGLE_VENDING_VERSION_CODE
                ? GOOGLE_VENDING_VERSION_CODE
                : vendingVersionCode;
    }

    public String getVendingVersionString(boolean defaultIfNotFound) {
        return defaultIfNotFound && vendingVersionCode < GOOGLE_VENDING_VERSION_CODE
                ? GOOGLE_VENDING_VERSION_STRING
                : vendingVersionString;
    }
}
