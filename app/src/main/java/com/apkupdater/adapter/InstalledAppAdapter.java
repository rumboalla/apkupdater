package com.apkupdater.adapter;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.view.InstalledAppView;
import com.apkupdater.view.InstalledAppView_;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class InstalledAppAdapter
	extends RecyclerView.Adapter<InstalledAppAdapter.InstalledAppViewHolder>
	implements View.OnLongClickListener
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> mApps;
	private Context mContext;
	private RecyclerView mView;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class InstalledAppViewHolder
		extends RecyclerView.ViewHolder
	{
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		private InstalledAppView mView;

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		InstalledAppViewHolder(
			InstalledAppView view
		) {
			super(view);
			mView = view;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		public void bind(
			InstalledApp app
		) {
			mView.bind(app);
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
		mView = view;
		mApps = sort(apps);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public InstalledAppViewHolder onCreateViewHolder(
		ViewGroup parent,
		int viewType
	) {
		InstalledAppView v = InstalledAppView_.build(mContext);
		v.setLayoutParams(new RecyclerView.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.WRAP_CONTENT
		));
		v.setOnLongClickListener(this);
		return new InstalledAppViewHolder(v);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private List<InstalledApp> sort(
		List<InstalledApp> items
	) {
		// Lists to hold both types of apps
		List<InstalledApp> normal = new ArrayList<>();
		List<InstalledApp> ignored = new ArrayList<>();

		// Get the ignore list
		UpdaterOptions options = new UpdaterOptions(mContext);
		List<String> ignore_list = options.getIgnoreList();

		// Iterate and buld the temp lists
		for (InstalledApp i : items) {
			if (ignore_list.contains(i.getPname())) {
				ignored.add(i);
			} else {
				normal.add(i);
			}
		}

		// Build comparator
		Comparator<InstalledApp> comparator = new Comparator<InstalledApp>() {
			@Override
			public int compare(InstalledApp o1, InstalledApp o2) {
				return o1.getName().compareToIgnoreCase(o2.getName());
			}
		};

		// Sort them
		Collections.sort(normal, comparator);
		Collections.sort(ignored, comparator);

		// Build final
		List<InstalledApp> ordered = new ArrayList<>();
		for (InstalledApp i : normal) {
			ordered.add(i);
		}

		for (InstalledApp i : ignored) {
			ordered.add(i);
		}

		return ordered;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onLongClick(
		View view
	) {
		// Get the ignore list from the options
		UpdaterOptions options = new UpdaterOptions(mContext);
		List<String> ignore_list = options.getIgnoreList();

		InstalledApp app = mApps.get(mView.getChildLayoutPosition(view));

		// If it's on the ignore remove, otherwise add it
		if (ignore_list.contains(app.getPname())) {
			ignore_list.remove(app.getPname());
		} else {
			ignore_list.add(app.getPname());
		}

		// Update the list
		options.setIgnoreList(ignore_list);

		// Sort it
		mApps = sort(mApps);

		notifyDataSetChanged();

		return true;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onBindViewHolder(
		InstalledAppViewHolder holder,
		int position
	) {
		holder.bind(mApps.get(position));
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