package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import com.apkupdater.event.InstallAppEvent;
import com.apkupdater.event.SnackBarEvent;
import com.apkupdater.model.AppState;
import com.apkupdater.model.DownloadInfo;
import com.apkupdater.model.InstallStatus;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.GooglePlayUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.PixelConversion;
import com.apkupdater.util.SnackBarUtil;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.GooglePlayException;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

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
    MyBus mBus;

    @Bean
    AppState mAppState;

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
            mName = mView.findViewById(R.id.installed_app_name);
            mPname = mView.findViewById(R.id.installed_app_pname);
            mVersion = mView.findViewById(R.id.installed_app_version);
            mIcon = mView.findViewById(R.id.installed_app_icon);
            mActionOneButton = mView.findViewById(R.id.action_one_button);
            mActionOneProgressBar = mView.findViewById(R.id.action_one_progressbar);

            mName.setText(app.getName());
            mPname.setText(app.getPname());
            mVersion.setText(app.getVersion());

            try {
                Drawable icon = mContext.getPackageManager().getApplicationIcon(app.getPname());
                mIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
            	mIcon.setImageResource(R.drawable.ic_android);
            }

            // Install button and its progress bar
            mActionOneButton.setVisibility(View.VISIBLE);
            mActionOneProgressBar.setVisibility(View.INVISIBLE);
			mActionOneButton.setOnClickListener(null);
            if (app.getInstallStatus().getStatus() == InstallStatus.STATUS_INSTALL) {
				mActionOneButton.setText(R.string.action_play);
				mActionOneButton.setOnClickListener(mAdapter);
			} else if (app.getInstallStatus().getStatus() == InstallStatus.STATUS_INSTALLED) {
				mActionOneButton.setText(R.string.action_installed);
			} else if (app.getInstallStatus().getStatus() == InstallStatus.STATUS_INSTALLING) {
				mActionOneProgressBar.setVisibility(View.VISIBLE);
				mActionOneButton.setVisibility(View.INVISIBLE);
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
		final int pos = mView.getChildAdapterPosition(parent);

        // Check if we are already installing
        if (app.getInstallStatus().getStatus() == InstallStatus.STATUS_INSTALLING) {
			return;
		} else {
			changeAppInstallStatusAndNotify(app, InstallStatus.STATUS_INSTALLING, 0, pos);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AndroidAppDeliveryData data = GooglePlayUtil.getAppDeliveryData(
						GooglePlayUtil.getApi(mContext),
						app.getPname()
					);

					long id = DownloadUtil.downloadFile(
						mContext,
						data.getDownloadUrl(),
						data.getDownloadAuthCookie(0).getName() + "=" + data.getDownloadAuthCookie(0).getValue(),
						app.getPname() + " " + app.getVersionCode()
					);

					mAppState.getDownloadInfo().put(
					    id,
                        new DownloadInfo(app.getPname(), app.getVersionCode(), app.getVersion())
                    );

					changeAppInstallStatusAndNotify(app, InstallStatus.STATUS_INSTALLING, id, pos);
                } catch (GooglePlayException gex) {
                    SnackBarUtil.make(mActivity, String.valueOf(gex.getMessage()));
                    mLog.log("SearchAdapter", String.valueOf(gex), LogMessage.SEVERITY_ERROR);
					changeAppInstallStatusAndNotify(app, InstallStatus.STATUS_INSTALL, 0, pos);
                } catch (Exception e) {
                    SnackBarUtil.make(mActivity, "Error downloading.");
                    mLog.log("SearchAdapter", String.valueOf(e), LogMessage.SEVERITY_ERROR);
					changeAppInstallStatusAndNotify(app, InstallStatus.STATUS_INSTALL, 0, pos);
                }
			}
		}).start();

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

        try {
			mBus.register(this);
		} catch (Exception ignored) {}
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread
	protected void changeAppInstallStatusAndNotify(
		final InstalledApp app,
		int status,
		long id,
		final int pos
	) {
		if (app.getInstallStatus() != null) {
			app.getInstallStatus().setId(id);
			app.getInstallStatus().setStatus(status);
			notifyItemChanged(pos);
		}
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Subscribe
    public void onInstallEvent(
        InstallAppEvent ev
    ) {
		for (int i = 0; i < mApps.size(); i++) {
			InstalledApp app = mApps.get(i);
			if (app.getInstallStatus().getId() == ev.getId() || app.getPname().equals(ev.getPackageName())) {
				app.getInstallStatus().setId(0);
				if (ev.isSuccess()) {
					app.getInstallStatus().setStatus(InstallStatus.STATUS_INSTALLED);
					mBus.post(new SnackBarEvent(mContext.getString(R.string.install_success)));
				} else {
					// If the app is already set as installed, do nothing
					if (app.getInstallStatus().getStatus() == InstallStatus.STATUS_INSTALLING) {
						app.getInstallStatus().setStatus(InstallStatus.STATUS_INSTALL);
						mBus.post(new SnackBarEvent(mContext.getString(R.string.install_failure)));
					}
				}
				notifyItemChanged(i);

				// Delete file
				DownloadUtil.deleteDownloadedFile(mContext, app.getInstallStatus().getId());
			}
		}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////