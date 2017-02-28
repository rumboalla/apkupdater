package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterUptodown
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String BaseUrl = "http://en.uptodown.com/android/";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterUptodown(
		Context context,
		String pname,
		String cversion
	) {
		super(context, pname, cversion, "Uptodown");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getUrl(
		String pname
	) {
		return BaseUrl + "search/" + pname.replace(".", "-");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String url
	) {
		try {
			Document doc = Jsoup.connect(url).get();

			for (int i = 0; i < 20; i++) {
				// Get the url for the first app returned
				Elements elements =  doc.getElementsByClass("cardlink_1_" + i);
				if (elements.size() < 1) {
					return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
				}
				String app_url = elements.get(0).attr("href");

				// Get package name from app url
				Document doc2 = Jsoup.connect(app_url).get();
				elements = doc2.getElementsByClass("packagename");
				if (elements.size() < 1) {
					return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
				}
				String pname = elements.get(0).getElementsByClass("right").get(0).html();

				// Check if it's the same app
				if (!pname.equals(mPname)) {
					continue;
				}

				// Get version
				String version = doc2.getElementsByAttributeValue("itemprop", "softwareVersion").get(0).html();

				// Compare versions
				if (compareVersions(mCurrentVersion, version) == -1) {
					mResultUrl = app_url;
					mResultVersion = version;
					return UpdaterStatus.STATUS_UPDATE_FOUND;
				}
			}

			return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
		} catch (HttpStatusException status) {
			if (status.getStatusCode() == 404 || status.getStatusCode() == 403) {
				return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
			} else {
				mError = addCommonInfoToError(status);
				return UpdaterStatus.STATUS_ERROR;
			}
		} catch (Exception e) {
			mError = addCommonInfoToError(e);
			return UpdaterStatus.STATUS_ERROR;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
