package de.apkgrabber.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import de.apkgrabber.R;
import de.apkgrabber.model.Constants;
import de.apkgrabber.model.IgnoreVersion;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterOptions
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	Context mContext;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterOptions(
		Context context
	) {
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean skipExperimental(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_skip_experimental_key), true);
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean selfUpdate(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_self_update_key), true);
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean checkUpdatesOnStartup(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_update_on_startup_key), false);
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean automaticInstall(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_play_automatic_install_key), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean disableAnimations(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_disable_animations_key), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean skipArchitecture(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_skip_architecture_key), true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean skipMinapi(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_skip_minapi_key), true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useAPKMirror(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_apkmirror_key), true);
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean useGooglePlay(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_play_key), false);
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useUptodown(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_uptodown_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useAPKPure(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_use_apkpure_key), false);
	}

	public boolean useAptoide() {
		SharedPreferences sharedPred = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPred.getBoolean(mContext.getString(R.string.preferences_general_use_aptoide_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<IgnoreVersion> getIgnoreVersionList(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		Gson g = new Gson();
		return g.fromJson(
			sharedPref.getString(mContext.getString(R.string.preferences_general_ignoreversionlist_key), "[]"),
			new TypeToken<List<IgnoreVersion>>(){}.getType()
		);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setIgnoreVersionList(
		List<IgnoreVersion> l
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		sharedPref.edit().putString(
			mContext.getString(R.string.preferences_general_ignoreversionlist_key),
			new Gson().toJson(l)
		).apply();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List<String> getIgnoreList(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		String s = sharedPref.getString(mContext.getString(R.string.preferences_general_ignorelist_key), mContext.getString(R.string.preferences_general_ignorelist_value));

		// Fill the list if it's not empty
		List<String> list = new ArrayList<>();
		//noinspection ConstantConditions
		if (s != null && !s.isEmpty()) { //s actually can be null, contrary to what AS says
			String [] strings = s.split(",");
			Collections.addAll(list, strings);
		}

		return list;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setIgnoreList(
		List<String> l
	) {
		// Make a comma separated string from the list
		String s = "";
		for(String i : l) {
			s += i + ",";
		}

		if (s.length() >= 2) {
			s = s.substring(0, s.length() - 1);
		}

		// Add it to shared prefs
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		sharedPref.edit().putString(
			mContext.getString(R.string.preferences_general_ignorelist_key),
			s
		).apply();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getNotificationOption(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_notification_key), mContext.getString(R.string.notification_always));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getAlarmOption(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_alarm_key), mContext.getString(R.string.alarm_daily));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean getWifiOnly(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_wifi_only_key), false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getNumThreads(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		String v = sharedPref.getString(mContext.getString(R.string.preferences_general_num_threads_key), mContext.getString(R.string.num_threads_five));
		return Integer.valueOf(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getTheme(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_theme_key), mContext.getString(R.string.theme_blue));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean getExcludeSystemApps(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_exclude_system_apps_key), true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean getExcludeDisabledApps(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_general_exclude_disabled_apps_key), true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getUpdateHour(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getString(mContext.getString(R.string.preferences_general_update_hour_key), mContext.getString(R.string.update_hour_twelve));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean useRootInstall(
	) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		return sharedPref.getBoolean(mContext.getString(R.string.preferences_play_root_install_key), false);
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean useOwnPlayAccount(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getBoolean(mContext.getString(R.string.preferences_play_own_account_key), false);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getOwnGsfId(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(Constants.OWN_GSFID_KEY, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setOwnGsfId(
        String id
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPref.edit().putString(Constants.OWN_GSFID_KEY, id).apply();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getOwnToken(
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPref.getString(Constants.OWN_TOKEN_KEY, null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setOwnToken(
        String id
    ) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedPref.edit().putString(Constants.OWN_TOKEN_KEY, id).apply();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
