package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.R;
import com.apkupdater.updater.UpdaterOptions;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class ThemeUtil
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int getActivityThemeFromOptions(
		Context context
	) {
		String theme = new UpdaterOptions(context).getTheme();
		if(theme.equals(context.getString(R.string.theme_blue))) {
			return R.style.AppThemeBlue;
		} else if (theme.equals(context.getString(R.string.theme_dark))) {
			return R.style.AppThemeDark;
		} else if (theme.equals(context.getString(R.string.theme_pink))) {
			return R.style.AppThemePink;
		} else if (theme.equals(context.getString(R.string.theme_orange))) {
            return R.style.AppThemeOrange;
        } else if (theme.equals(context.getString(R.string.theme_bloody))) {
            return R.style.AppThemeBloody;
		} else if (theme.equals(context.getString(R.string.theme_amoled))) {
			return R.style.AppThemeAmoled;
		} else {
			return R.style.AppThemeBlue;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static int getSettingsThemeFromOptions(
		Context context
	) {
		String theme = new UpdaterOptions(context).getTheme();
		if(theme.equals(context.getString(R.string.theme_blue))) {
			return R.style.PreferenceThemeBlue;
		} else if (theme.equals(context.getString(R.string.theme_dark))) {
			return R.style.PreferenceThemeDark;
		} else if (theme.equals(context.getString(R.string.theme_pink))) {
			return R.style.PreferenceThemePink;
		}else if (theme.equals(context.getString(R.string.theme_orange))) {
			return R.style.PreferenceThemeOrange;
        } else if (theme.equals(context.getString(R.string.theme_bloody))) {
            return R.style.PreferenceThemeBloody;
		} else if (theme.equals(context.getString(R.string.theme_amoled))) {
			return R.style.PreferenceThemeAmoled;
		} else {
			return R.style.PreferenceThemeBlue;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static int getCardBackgroundColor(
        Context context
    ) {
        String theme = new UpdaterOptions(context).getTheme();
        if(theme.equals(context.getString(R.string.theme_blue))) {
            return 0xFFFFFFFF;
        } else if (theme.equals(context.getString(R.string.theme_dark))) {
            return 0xFF424242;
        } else if (theme.equals(context.getString(R.string.theme_pink))) {
            return 0xFF424242;
        }else if (theme.equals(context.getString(R.string.theme_orange))) {
            return 0xFFFFFFFF;
        } else if (theme.equals(context.getString(R.string.theme_bloody))) {
            return 0;
		} else if (theme.equals(context.getString(R.string.theme_amoled))) {
			return 0;
		} else {
            return 0xFFFFFFFF;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
