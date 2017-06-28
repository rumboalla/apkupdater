package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EBean(scope = EBean.Scope.Singleton)
public class InstalledAppUtil
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<InstalledApp> getInstalledApps(
		Context context
	) {
		UpdaterOptions options = new UpdaterOptions(context);
		PackageManager pm = context.getPackageManager();
		ArrayList<InstalledApp> items = new ArrayList<>();
		List<PackageInfo> apps = pm.getInstalledPackages(0);

		for (PackageInfo i : apps) {
			// Check it it's a system app
			if (options.getExcludeSystemApps() && (i.applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
				continue;
			}

			if (options.getExcludeDisabledApps()){
				try {
					ApplicationInfo ai = pm.getApplicationInfo(i.packageName, 0);
					if (!ai.enabled){
						continue;
					}
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}
			}

			// Check if the app was installed by something (Google Play, Amazon, etc)
			String installer = pm.getInstallerPackageName(i.packageName);
			if (installer != null && !installer.isEmpty()) {
				//continue;
			}

			// Get the data and add it to the list
			InstalledApp app = new InstalledApp();
			app.setName(i.applicationInfo.loadLabel(pm).toString());
			app.setPname(i.packageName);
			app.setVersion(i.versionName);
            app.setVersionCode(i.versionCode);
			items.add(app);
		}

		return items;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Background
	public void getInstalledAppsAsync(
		final Context context,
		final GenericCallback<List<InstalledApp>> callback
	) {
		List<InstalledApp> items = getInstalledApps(context);
		callback.onResult(items);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static public List<InstalledApp> sort(
		Context context,
		List<InstalledApp> items
	) {
		if (items == null) {
			return null;
		}

		// Lists to hold both types of apps
		List<InstalledApp> normal = new ArrayList<>();
		List<InstalledApp> ignored = new ArrayList<>();

		// Get the ignore list
		UpdaterOptions options = new UpdaterOptions(context);
		List<String> ignore_list = options.getIgnoreList();

		// Iterate and buld the temp lists
		for (InstalledApp i : items) {
			if (ignore_list.contains(i.getPname())) {
				ignored.add(i);
			} else {
				normal.add(i);
			}
		}

		// Build comparator
		Comparator<InstalledApp> comparator = new Comparator<InstalledApp>() {
			@Override
			public int compare(InstalledApp o1, InstalledApp o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};

		// Sort them
		Collections.sort(normal, comparator);
		Collections.sort(ignored, comparator);

		// Build final
		List<InstalledApp> ordered = new ArrayList<>();
		for (InstalledApp i : normal) {
			ordered.add(i);
		}

		for (InstalledApp i : ignored) {
			ordered.add(i);
		}

		return ordered;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public int getAppVersionCode(
        Context context,
        String pName
    ) {
	    try {
            PackageManager pm = context.getPackageManager();
            PackageInfo i = pm.getPackageInfo(pName, 0);
            return i.versionCode;
        } catch (Exception e) {
            return 0;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public String getAppVersionName(
        Context context,
        String pName
    ) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo i = pm.getPackageInfo(pName, 0);
            return i.versionName;
        } catch (Exception e) {
            return null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////