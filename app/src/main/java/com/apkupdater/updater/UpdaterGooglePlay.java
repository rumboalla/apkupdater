package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.util.VersionUtil;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterGooglePlay
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String BaseUrl = "https://play.google.com/store/apps/details?id=";
	static final private String DownloadUrl = "https://apps.evozi.com/apk-downloader/?id=";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterGooglePlay(
		Context context,
		String pname,
		String cversion
	) {
		super(context, pname, cversion, "GooglePlay");
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getUrl(
		String pname
	) {
		return BaseUrl + pname;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String url
	) {
		try {
			Document doc = Jsoup.connect(url).get();

			// Check if no results
			Elements elements = doc.getElementsByAttributeValue("itemprop", "softwareVersion");
			if(elements == null || elements.size() == 0) {
				return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
			}

			if (compareVersions(mCurrentVersion, elements.get(0).text()) == -1) {
				mResultUrl = DownloadUrl + mPname;
				return UpdaterStatus.STATUS_UPDATE_FOUND;
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