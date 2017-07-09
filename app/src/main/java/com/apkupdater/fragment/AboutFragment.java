package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.Constants;
import com.apkupdater.util.ColorUtil;
import com.apkupdater.util.DownloadUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_about)
public class AboutFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.app_name_text)
	protected TextView mAppNameText;

	@ViewById(R.id.app_version_text)
	protected TextView mAppVersionText;

	@ViewById(R.id.text_container)
	protected FrameLayout mTextContainer;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private WebView getWebView(
	) {
		final WebView webView = new WebView(getContext());

		webView.loadData(getString(R.string.app_description_html), "text/html", "UTF-8");
		webView.setBackgroundColor(0x00000000);
		webView.getSettings().setDefaultFontSize(14);

		// Change the webview font color
		final String color = ColorUtil.getHexStringFromInt( mAppVersionText.getTextColors().getDefaultColor());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			public void onPageFinished(WebView view, String url) {
				try {
					webView.loadUrl(
						"javascript:document.body.style.setProperty(\"color\", \"" + color + "\");"
					);
				} catch (Exception ignored) {
				}
			}
		});

		return webView;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private TextView getTextView(
	) {
		TextView textView = new TextView(getContext());
		Spanned fromHtml;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			fromHtml = Html.fromHtml(getString(R.string.app_description_html), 0);
		}else{
			//noinspection deprecation
			fromHtml = Html.fromHtml(getString(R.string.app_description_html));
		}
		textView.setText(fromHtml);
		textView.setMovementMethod (LinkMovementMethod.getInstance());
		return textView;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	protected void onInit(
	) {
		mAppNameText.setTextColor(ColorUtil.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent));
		mAppNameText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DownloadUtil.launchBrowser(getContext(), Constants.GitHubURL);
			}
		});

		try {
			WebView v = getWebView();
			ViewCompat.setNestedScrollingEnabled(v, true);
			mTextContainer.addView(v);
		} catch (Exception e) {
			try {
				TextView v = getTextView();
				ViewCompat.setNestedScrollingEnabled(v, true);
				mTextContainer.addView(v);
			} catch (Exception ignored) {

			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
