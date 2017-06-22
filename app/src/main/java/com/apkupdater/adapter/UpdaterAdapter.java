package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.LogMessage;
import com.apkupdater.model.Update;
import com.apkupdater.updater.UpdaterGooglePlay;
import com.apkupdater.util.ColorUtil;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.PixelConversion;
import com.apkupdater.util.SnackBarUtil;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.GooglePlayException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EBean
public class UpdaterAdapter
	extends RecyclerView.Adapter<UpdaterAdapter.UpdateViewHolder>
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<Update> mUpdates;
	private Context mContext;
	private RecyclerView mView;
    private Activity mActivity;

    @Bean
    MyBus mBus;

    @Bean
    LogUtil mLog;

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
        private ProgressBar mActionOneProgressBar;

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
            mActionOneProgressBar = ((ProgressBar) mView.findViewById(R.id.action_one_progressbar));

			// Set values
			mName.setText(update.getName());
			mPname.setText(update.getPname());

			// Build version string with both old and new version
			String version = update.getVersion();
			if (update.getNewVersion() != null && !update.getNewVersion().isEmpty()) {
			    String newCode = update.getNewVersionCode() == 0 ? "?" : String.valueOf(update.getNewVersionCode());
				version += " (" + update.getVersionCode() + ") -> " + update.getNewVersion() + " (" + newCode  + ")";
			}

			mVersion.setText(version);

			// Build string for first action
			String action = "";
			String action2 = "";

			if (update.getUrl().contains("apkmirror.com")) {
				action = mContext.getString(R.string.action_apkmirror);
				action2 = mContext.getString(R.string.action_evozi);
			} else if (update.getUrl().contains("uptodown.com")) {
				action = mContext.getString(R.string.action_uptodown);
                action2 = mContext.getString(R.string.action_evozi);
			} else if (update.getUrl().contains("apkpure.com")) {
				action = mContext.getString(R.string.action_apkpure);
                action2 = mContext.getString(R.string.action_evozi);
			} else if (update.getCookie() != null) {
			    action = mContext.getString(R.string.action_play);
            }
			mActionOneButton.setText(action);
            mActionTwoButton.setText(action2);

			// Action 1 listener
            if (!action.isEmpty()) {
                mActionOneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mActionOneButton.getText().equals(mContext.getString(R.string.action_play))) {
                            setProgressBarVisibility(View.VISIBLE);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        GooglePlayAPI api = UpdaterGooglePlay.getGooglePlayApi(mContext);
                                        DocV2 d = api.details(update.getPname()).getDocV2();
                                        AndroidAppDeliveryData data = api.purchase(
                                            d.getDetails().getAppDetails().getPackageName(),
                                            d.getDetails().getAppDetails().getVersionCode(),
                                            d.getOffer(0).getOfferType()
                                        ).getPurchaseStatusResponse().getAppDeliveryData();

                                        DownloadUtil.downloadFile(
                                            mContext,
                                            data.getDownloadUrl(),
                                            data.getDownloadAuthCookie(0).getName() + "=" + data.getDownloadAuthCookie(0).getValue(),
                                            update.getPname() + " " + update.getVersionCode()
                                        );

                                        setProgressBarVisibility(View.GONE); // TODO: Implement download/install notification
                                    } catch (GooglePlayException gex) {
                                        SnackBarUtil.make(mActivity, String.valueOf(gex.getMessage()));
                                        mLog.log("UpdaterAdapter", String.valueOf(gex), LogMessage.SEVERITY_ERROR);
                                        setProgressBarVisibility( View.GONE);
                                    } catch (Exception e) {
                                        SnackBarUtil.make(mActivity, "Error downloading.");
                                        mLog.log("UpdaterAdapter", String.valueOf(e), LogMessage.SEVERITY_ERROR);
                                        setProgressBarVisibility(View.GONE);
                                    }
                                }
                            }).start();

                        } else {
                            DownloadUtil.LaunchBrowser(mContext, update.getUrl());
                        }
                    }
                });
            }

			// Action2 evozi listener
            if (!action2.isEmpty()) {
                mActionTwoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                            "https://apps.evozi.com/apk-downloader/?id=" + update.getPname()
                        ));
                        mContext.startActivity(browserIntent);
                    }
                });
            }

			// Icon
			try {
				Drawable icon = mView.getContext().getPackageManager().getApplicationIcon(update.getPname());
				mIcon.setImageDrawable(icon);
			} catch (PackageManager.NameNotFoundException ignored) {}

			// Beta icon
			mIsBetaIcon.setVisibility(update.isBeta() ? View.VISIBLE : View.GONE);
			mIsBetaIcon.getBackground().setColorFilter(
				ColorUtil.getColorFromTheme(mContext.getTheme(), R.attr.colorAccent),
				android.graphics.PorterDuff.Mode.MULTIPLY
			);

			setTopMargin(0);
		}

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void setProgressBarVisibility(
            final int visibility
        ) {
            if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
                mActionOneProgressBar.setVisibility(visibility);
                mActionOneButton.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
            } else {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActionOneProgressBar.setVisibility(visibility);
                        mActionOneButton.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                    }
                });
            }
        }

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void setTopMargin(
            int margin
        ) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            params.topMargin = (int) PixelConversion.convertDpToPixel(margin, mContext);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public UpdaterAdapter(
		Context context
	) {
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public UpdateViewHolder onCreateViewHolder(
		ViewGroup parent,
		int viewType
	) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.updater_item, parent, false);
		return new UpdateViewHolder(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onBindViewHolder(
		UpdateViewHolder holder,
		int position
	) {
		holder.bind(mUpdates.get(position));

        if (position == 0) {
		    holder.setTopMargin(8);
        }
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

		int index = mUpdates.indexOf(update);

		notifyItemChanged(0);
		notifyItemInserted(index);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setUpdates(
		List<Update> updates
	) {
		mUpdates = updates;
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

    public void init(
        Activity activity,
        RecyclerView view,
        List<Update> updates
    ) {
        mActivity = activity;
        mView = view;
        setUpdates(updates);
        mBus.register(this);
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////