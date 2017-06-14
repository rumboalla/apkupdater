package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterGooglePlay;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.DownloadUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.PixelConversion;
import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class SearchAdapter
	extends RecyclerView.Adapter<SearchAdapter.InstalledAppViewHolder>
	implements View.OnClickListener
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> mApps;
	private Context mContext;
	private RecyclerView mView;
    private SearchAdapter mAdapter;

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
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public SearchAdapter(
		Context context,
		RecyclerView view,
		List<InstalledApp> apps
	) {
		mContext = context;
		mAdapter = this;
		mView = view;
		AnimationUtil.startListAnimation(mView);
		mApps = InstalledAppUtil.sort(mContext, apps);
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
				} catch (Exception e) {

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

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////