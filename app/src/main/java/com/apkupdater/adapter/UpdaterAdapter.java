package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.Update;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.DownloadUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.DOWNLOAD_SERVICE;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class UpdaterAdapter
	extends RecyclerView.Adapter<UpdaterAdapter.UpdateViewHolder>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<Update> mUpdates;
	private Context mContext;
	private RecyclerView mView;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class UpdateViewHolder
		extends RecyclerView.ViewHolder
	{
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		private View mView;
		private TextView mName;
		private TextView mPname;
		private TextView mVersion;
		private TextView mUrl;
		private ImageView mIcon;
		private Button mActionOneButton;
		private Button mActionTwoButton;
		private ImageView mIsBetaIcon;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		UpdateViewHolder(
			View view
		) {
			super(view);
			mView = view;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		public void bind(
			final Update update
		) {
			// Get views
			mName = ((TextView) mView.findViewById(R.id.installed_app_name));
			mPname = ((TextView) mView.findViewById(R.id.installed_app_pname));
			mVersion = ((TextView) mView.findViewById(R.id.installed_app_version));
			mIcon = ((ImageView) mView.findViewById(R.id.installed_app_icon));
			mIsBetaIcon = ((ImageView) mView.findViewById(R.id.isbeta_icon));
			mUrl = ((TextView) mView.findViewById(R.id.update_url));
			mActionOneButton = ((Button) mView.findViewById(R.id.action_one_button));
			mActionTwoButton = ((Button) mView.findViewById(R.id.action_two_button));

			// Set values
			mName.setText(update.getName());
			mPname.setText(update.getPname());

			// Build version string with both old and new version
			String version = update.getVersion();
			if (update.getNewVersion() != null && !update.getNewVersion().isEmpty()) {
			    String newCode = update.getNewVersionCode() == 0 ? "?" : String.valueOf(update.getNewVersionCode());
				version += "(" + update.getVersionCode() + ") -> " + update.getNewVersion() + " (" + newCode  + ")";
			}

			mVersion.setText(version);

			// Build string for first action
			String action = "Error";
			if (update.getUrl().contains("apkmirror.com")) {
				action = mContext.getString(R.string.action_apkmirror);
			} else if (update.getUrl().contains("uptodown.com")) {
				action = mContext.getString(R.string.action_uptodown);
			} else if (update.getUrl().contains("apkpure.com")) {
				action = mContext.getString(R.string.action_apkpure);
			} else if (update.getCookie() != null) {
			    action = mContext.getString(R.string.action_play);
            }
			mActionOneButton.setText(action);

			// Action 1 listener
			mActionOneButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
				    if (mActionOneButton.getText().equals(mContext.getString(R.string.action_play))) {
				        DownloadUtil.downloadFile(
				            mContext,
                            update.getUrl(),
                            update.getCookie(),
                            update.getPname() + " " + update.getNewVersion()
                        );
                    } else {
                        DownloadUtil.LaunchBrowser(mContext, update.getUrl());
                    }
				}
			});

			// Action2 evozi listener
			mActionTwoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
						"https://apps.evozi.com/apk-downloader/?id=" + update.getPname()
					));
					mContext.startActivity(browserIntent);
				}
			});

			// Icon
			try {
				Drawable icon = mView.getContext().getPackageManager().getApplicationIcon(update.getPname());
				mIcon.setImageDrawable(icon);
			} catch (PackageManager.NameNotFoundException ignored) {}

			// Beta icon
			mIsBetaIcon.setVisibility(update.isBeta() ? View.VISIBLE : View.GONE);
			mIsBetaIcon.getBackground().setColorFilter(
				ColorUtitl.getColorFromTheme(mContext.getTheme(), R.attr.colorAccent),
				android.graphics.PorterDuff.Mode.MULTIPLY
			);
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterAdapter(
		Context context,
		RecyclerView view,
		List<Update> apps
	) {
		mContext = context;
		mView = view;

		mUpdates = apps;
		sort();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public UpdateViewHolder onCreateViewHolder(
		ViewGroup parent,
		int viewType
	) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.updater_item, parent, false);
		/*
		v.setLayoutParams(new RecyclerView.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		));
		*/
		return new UpdateViewHolder(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onBindViewHolder(
		UpdateViewHolder holder,
		int position
	) {
		holder.bind(mUpdates.get(position));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getItemCount(
	) {
		return mUpdates.size();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void addUpdate(
		Update update
	) {
		mUpdates.add(update);
		sort();
		notifyItemInserted(mUpdates.indexOf(update));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setUpdates(
		List<Update> updates
	) {
		mUpdates.clear();
		mUpdates.addAll(updates);
		sort();
		notifyDataSetChanged();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public int getCount(
	) {
		return mUpdates.size();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void sort(
	) {
		// Sorting non beta first, then alphabetically
		Collections.sort(mUpdates, new Comparator<Update>() {
			@Override
			public int compare(Update u1, Update u2) {
				return u1.isBeta() == u2.isBeta() ? u1.getName().compareToIgnoreCase(u2.getName()) : u1.isBeta() ? 1 : -1;
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////