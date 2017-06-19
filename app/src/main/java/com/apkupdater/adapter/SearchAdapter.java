package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.event.DownloadCompleteEvent;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.updater.UpdaterGooglePlay;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.InstalledAppUtil;
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

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EBean
public class SearchAdapter
	extends RecyclerView.Adapter<SearchAdapter.InstalledAppViewHolder>
	implements View.OnClickListener
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> mApps;
	private Context mContext;
	private RecyclerView mView;
    private SearchAdapter mAdapter;
    private Activity mActivity;

    @Bean
    LogUtil mLog;

    @Bean
    MyBus myBus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class InstalledAppViewHolder
		extends RecyclerView.ViewHolder
	{
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		private View mView;
        private TextView mName;
        private TextView mPname;
        private TextView mVersion;
        private ImageView mIcon;
        private Button mActionOneButton;
        private ProgressBar mActionOneProgressBar;
        private long dowloadId;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		InstalledAppViewHolder(
			View view
		) {
			super(view);
			mView = view;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		public void bind(
			InstalledApp app
		) {
		    // Get views
            mName = ((TextView) mView.findViewById(R.id.installed_app_name));
            mPname = ((TextView) mView.findViewById(R.id.installed_app_pname));
            mVersion = ((TextView) mView.findViewById(R.id.installed_app_version));
            mIcon = ((ImageView) mView.findViewById(R.id.installed_app_icon));
            mActionOneButton = ((Button) mView.findViewById(R.id.action_one_button));
            mActionOneProgressBar = ((ProgressBar) mView.findViewById(R.id.action_one_progressbar));

            mName.setText(app.getName());
            mPname.setText(app.getPname());
            mVersion.setText(app.getVersion());

            try {
                Drawable icon = mContext.getPackageManager().getApplicationIcon(app.getPname());
                mIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
            	mIcon.setImageResource(R.drawable.ic_android);
            }

            mActionOneButton.setText(R.string.action_play);
            mActionOneButton.setOnClickListener(mAdapter);
		}

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        private void setTopMargin(
            int margin
        ) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mView.getLayoutParams();
            params.topMargin = (int) PixelConversion.convertDpToPixel(margin, mContext);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        public void setProgressBarVisibility(
            int visibility
        ) {
		    mActionOneProgressBar.setVisibility(visibility);
		    mActionOneButton.setVisibility(visibility == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        public void setDownloadId(
            long id
        ) {
		    dowloadId = id;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        public long getDownloadId(
        ) {
		    return dowloadId;
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public SearchAdapter(
		Context context
	) {
		mContext = context;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public InstalledAppViewHolder onCreateViewHolder(
		ViewGroup parent,
		int viewType
	) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.installed_app_item, parent, false);
		return new InstalledAppViewHolder(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private CardView getInstalledAppViewParent(
		View v
	) {
		while (v != null) {
			v = (View) v.getParent();
			if (v instanceof CardView) {
				return (CardView) v;
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onClick(
		View view
	) {
		// Get the InstalledAppView parent
        CardView parent = getInstalledAppViewParent(view);
		if (parent == null) {
			return;
		}

		final InstalledApp app = mApps.get(mView.getChildLayoutPosition(parent));
        final InstalledAppViewHolder holder = (InstalledAppViewHolder) mView.getChildViewHolder(parent);
        setProgressBarVisibility(holder, View.VISIBLE);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					GooglePlayAPI api = UpdaterGooglePlay.getGooglePlayApi(mContext);
					DocV2 d = api.details(app.getPname()).getDocV2();
					AndroidAppDeliveryData data = api.purchase(
						d.getDetails().getAppDetails().getPackageName(),
						d.getDetails().getAppDetails().getVersionCode(),
						d.getOffer(0).getOfferType()
					).getPurchaseStatusResponse().getAppDeliveryData();

					DownloadUtil.downloadFile(
						mContext,
						data.getDownloadUrl(),
						data.getDownloadAuthCookie(0).getName() + "=" + data.getDownloadAuthCookie(0).getValue(),
						app.getPname() + " " + app.getVersionCode()
					);

                    setProgressBarVisibility(holder, View.GONE); // TODO: Implement download/install notification
                } catch (GooglePlayException gex) {
                    SnackBarUtil.make(mActivity, String.valueOf(gex.getMessage()));
                    mLog.log("SearchAdapter", String.valueOf(gex), LogMessage.SEVERITY_ERROR);
                    setProgressBarVisibility(holder, View.GONE);
                } catch (Exception e) {
                    SnackBarUtil.make(mActivity, "Error downloading.");
                    mLog.log("SearchAdapter", String.valueOf(e), LogMessage.SEVERITY_ERROR);
                    setProgressBarVisibility(holder, View.GONE);
                }
			}
		}).start();

	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setProgressBarVisibility(
	    final InstalledAppViewHolder holder,
	    final int visibility
    ) {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            holder.setProgressBarVisibility(visibility);
        } else {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    holder.setProgressBarVisibility(visibility);
                }
            });
        }
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onBindViewHolder(
		InstalledAppViewHolder holder,
		int position
	) {
		holder.bind(mApps.get(position));

        if (position == 0) {
            holder.setTopMargin(8);
        }
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public int getItemCount(
	) {
		return mApps.size();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void init(
        Activity activity,
        RecyclerView view,
        List<InstalledApp> apps
    ) {
	    mActivity = activity;
        mAdapter = this;
        mView = view;
        mApps = InstalledAppUtil.sort(mContext, apps);

        myBus.register(this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void onDownloadComplete(
        DownloadCompleteEvent ev
    ) {
	    // TODO: Implement
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////