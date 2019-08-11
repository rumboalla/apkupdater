package de.apkgrabber.updater;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAptoide
	extends UpdaterBase
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	static final private String BaseUrl = "https://ws75.aptoide.com";
	static final private String Type = "Aptoide";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterAptoide(
		Context context,
		String pname,
		String cversion
	) {
		super(context, pname, cversion, Type);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected String getUrl(
		String pname
	) {
		return BaseUrl + "/api/7/app/get/package_name=" + pname;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected UpdaterStatus parseUrl(
		String url
	) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			final JsonNode file = mapper.readTree(new URL(url)).get("nodes").get("meta").get("data").get("file");
			final String versionName = file.get("vername").asText();
			final int versionCode = file.get("vercode").asInt();
			final String apkUrl = file.get("path").asText();
			
			if (compareVersions(mCurrentVersion, versionName) == -1) {
				mResultUrl = apkUrl;
				mResultVersion = versionName;
				mResultVersionCode = versionCode;
				return UpdaterStatus.STATUS_UPDATE_FOUND;
			}
		} catch (Exception e) {
			mError = addCommonInfoToError(e);
			return UpdaterStatus.STATUS_ERROR;
		}

		return UpdaterStatus.STATUS_UPDATE_NOT_FOUND;
	}
}
