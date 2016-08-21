package com.apkupdater.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.apkupdater.util.VersionUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAPKPure
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String BaseUrl = "https://apkpure.com";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterAPKPure(
		Context context,
		String pname,
		String cversion
	) {
		super(context, pname, cversion);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getUrl(
		String pname
	) {
		return BaseUrl + "/search?q=" + pname;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String url
	) {
		try {
			Document doc = Jsoup.connect(url).get();

			// Search for URL ending on / + package name
			Elements elements = doc.getElementsByAttributeValueEnding("href", "/" + mPname);
			if(elements == null || elements.size() == 0) {
				return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
			}

			// Build new url to request
			url = BaseUrl + elements.get(0).attr("href");
			doc = Jsoup.connect(url).get();

			// Try to get the version
			elements = doc.getElementsByAttributeValue("itemprop", "softwareVersion");
			if(elements == null || elements.size() == 0) {
				return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
			}

			// Check the version
			int r = VersionUtil.compareVersion(
				VersionUtil.getVersionFromString(mCurrentVersion),
				VersionUtil.getVersionFromString(elements.get(0).text())
			);

			// If version is old, report update
			if (r == -1) {
				mResultUrl = url;
				return UpdaterStatus.STATUS_UPDATE_FOUND;
			}

			return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
		} catch (Exception e) {
			mError = e;
			return UpdaterStatus.STATUS_ERROR;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////