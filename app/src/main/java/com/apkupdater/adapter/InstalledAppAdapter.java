package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
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
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.AnimationUtil;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.PixelConversion;

import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class InstalledAppAdapter
	extends RecyclerView.Adapter<InstalledAppAdapter.InstalledAppViewHolder>
	implements View.OnClickListener
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> mApps;
	private Context mContext;
	private RecyclerView mView;
    private InstalledAppAdapter mAdapter;

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

            // Make the ignore overlay visible if this app is on the ignore list
            UpdaterOptions options = new UpdaterOptions(mContext);
            if (options.getIgnoreList().contains(app.getPname())) {
                ViewCompat.setAlpha(mView, 0.50f);
                mActionOneButton.setText(R.string.action_unignore_app);
            } else {
                ViewCompat.setAlpha(mView, 1.00f);
                mActionOneButton.setText(R.string.action_ignore_app);
            }

            try {
                Drawable icon = mContext.getPackageManager().getApplicationIcon(app.getPname());
                mIcon.setImageDrawable(icon);
            } catch (PackageManager.NameNotFoundException e) {
            	mIcon.setImageResource(R.drawable.ic_android);
            }

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

	public InstalledAppAdapter(
		Context context,
		RecyclerView view,
		List<InstalledApp> apps
	) {
		mContext = context;
		mAdapter = this;
		mView = view;
		AnimationUtil.startSlideAnimation(mContext, mView);
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
		// Get the ignore list from the options
		UpdaterOptions options = new UpdaterOptions(mContext);
		List<String> ignore_list = options.getIgnoreList();

		// Get the InstalledAppView parent
        CardView parent = getInstalledAppViewParent(view);
		if (parent == null) {
			return;
		}

		InstalledApp app = mApps.get(mView.getChildLayoutPosition(parent));

		// If it's on the ignore remove, otherwise add it
		if (ignore_list.contains(app.getPname())) {
			ignore_list.remove(app.getPname());
		} else {
			ignore_list.add(app.getPname());
		}

		// Update the list
		options.setIgnoreList(ignore_list);

		// Sort it
		AnimationUtil.startSlideAnimation(mContext, mView);
		mApps = InstalledAppUtil.sort(mContext, mApps);
		notifyDataSetChanged();
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