package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.util.VersionUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAPKMirror
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String BaseUrl = "http://www.apkmirror.com";
	static final private String Type = "APKMirror";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterAPKMirror(
		Context context,
		String pname,
	    String cversion
	) {
		super(context, pname, cversion, "APKMirror");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getUrl(
		String pname
	) {
		return BaseUrl + "/?s=" + pname + "&post_type=app_release&searchtype=apk";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String url
	) {
		try {
			Document doc = Jsoup.connect(url).get();

			// Check if no results
			Elements elements = doc.getElementsByClass("addpadding");
			if(elements != null && elements.size() > 0) {
				return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
			}

			// Check all the approws with apps
			for (Element row: doc.getElementsByClass("listWidget").get(0).getElementsByClass("appRow")) {
				try {
					String app = row.getElementsByTag("h5").get(0).attr("title");

					if (VersionUtil.isExperimental(app) && skipExperimental()) {
						continue;
					}

					if (compareVersions(mCurrentVersion, app) == -1) {
						mResultUrl = BaseUrl + row.getElementsByTag("a").get(0).attr("href");
						return UpdaterStatus.STATUS_UPDATE_FOUND;
					}
				} catch (Exception e) {
					mError = addCommonInfoToError(e);
					return UpdaterStatus.STATUS_ERROR;
				}
			}
			return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
		} catch (Exception e) {
			mError = addCommonInfoToError(e);
			return UpdaterStatus.STATUS_ERROR;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////