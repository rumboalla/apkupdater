package com.apkupdater.util;


import android.content.Context;

import com.apkupdater.R;
import com.apkupdater.updater.UpdaterOptions;

public class ThemeUtil
{
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
		}else if (theme.equals(context.getString(R.string.theme_orange))) {
			return R.style.AppThemeOrange;
		} else {
			return R.style.AppThemeBlue;
		}
	}

}
