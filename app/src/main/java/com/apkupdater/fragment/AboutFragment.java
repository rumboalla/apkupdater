package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Build;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.apkupdater.BuildConfig;
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

	@ViewById(R.id.app_name_text)
	protected TextView mAppNameText;

	@ViewById(R.id.app_version_text)
	protected TextView mAppVersionText;

	@ViewById(R.id.app_readme_text)
	protected TextView mAppReadmeText;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	protected void onInit(
	) {
		mAppNameText.setTextColor(ColorUtitl.getColorFromTheme(getActivity().getTheme(), R.attr.colorAccent));
		mAppVersionText.setText(BuildConfig.VERSION_NAME);

		Spanned fromHtml;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			fromHtml = Html.fromHtml(getString(R.string.app_description_html), 0);
		}else{
			//noinspection deprecation
			fromHtml = Html.fromHtml(getString(R.string.app_description_html));
		}
		mAppReadmeText.setText(fromHtml);
		mAppReadmeText.setMovementMethod (LinkMovementMethod.getInstance());

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
