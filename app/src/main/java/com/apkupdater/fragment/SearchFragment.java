package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.adapter.InstalledAppAdapter;
import com.apkupdater.adapter.SearchAdapter;
import com.apkupdater.event.InstalledAppTitleChange;
import com.apkupdater.event.SearchTitleChange;
import com.apkupdater.event.UpdateInstalledAppsEvent;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.updater.UpdaterGooglePlay;
import com.apkupdater.util.ColorUtitl;
import com.apkupdater.util.GenericCallback;
import com.apkupdater.util.InstalledAppUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.ThemeUtil;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.SearchIterator;
import com.github.yeriomin.playstoreapi.SearchResponse;
import com.github.yeriomin.playstoreapi.SearchSuggestEntry;
import com.github.yeriomin.playstoreapi.SearchSuggestResponse;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@EFragment(R.layout.fragment_search)
public class SearchFragment
	extends Fragment
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@ViewById(R.id.list_view)
	RecyclerView mRecyclerView;

	@ViewById(R.id.input_search)
    EditText mInputSearch;

	@Bean
	MyBus mBus;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mBus.register(this);

        mInputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(mInputSearch.getText().toString());
                    return true;
                }
                return false;
            }
        });


        //updateInstalledApps(new UpdateInstalledAppsEvent());
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy() {
		mBus.unregister(this);
		super.onDestroy();
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void search(
        final String text
    ) {
        // TODO: Implement
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GooglePlayAPI api = UpdaterGooglePlay.getGooglePlayApi(getContext());

                    SearchIterator i = new SearchIterator(api, text);
                    SearchResponse r = i.next();

                    List<InstalledApp> apps = new ArrayList<>();
                    for (DocV2 d : r.getDoc(0).getChildList()) {
                        InstalledApp app = new InstalledApp();
                        app.setPname(d.getDetails().getAppDetails().getPackageName());
                        app.setVersionCode(d.getDetails().getAppDetails().getVersionCode());
                        app.setVersion(d.getDetails().getAppDetails().getVersionString());
                        app.setName(d.getTitle());
                        apps.add(app);
                    }

                    setListAdapter(apps);
                } catch (Exception e) {
                    return;
                }
            }
        }).start();
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	protected void setListAdapter(
		List<InstalledApp> items
	) {
		if (mRecyclerView == null || mBus == null) {
			return;
		}

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setAdapter(new SearchAdapter(getContext(), mRecyclerView, items));

		mBus.post(new SearchTitleChange(getString(R.string.tab_search) + " (" + items.size() + ")"));
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
