package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.support.v4.app.Fragment;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.util.ColorUtitl;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_about)
public class AboutFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.web_view)
	protected WebView mWebView;

	@ViewById(R.id.app_name_text)
	protected TextView mAppNameText;

	@ViewById(R.id.app_version_text)
	protected TextView mAppVersionText;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	protected void onInit(
	) {
		mAppNameText.setTextColor(ColorUtitl.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent));
		mWebView.loadData(getString(R.string.app_description_html), "text/html", "UTF-8");
		mWebView.setBackgroundColor(0x00000000);
		mWebView.getSettings().setDefaultFontSize(14);

		// Change the webview font color
		final String color = ColorUtitl.getHexStringFromInt( mAppVersionText.getTextColors().getDefaultColor());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				mWebView.loadUrl(
					"javascript:document.body.style.setProperty(\"color\", \"" + color + "\");"
				);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
