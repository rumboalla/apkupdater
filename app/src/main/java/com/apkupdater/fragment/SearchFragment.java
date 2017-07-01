package com.apkupdater.fragment;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apkupdater.R;
import com.apkupdater.adapter.SearchAdapter;
import com.apkupdater.event.SearchTitleChange;
import com.apkupdater.model.InstalledApp;
import com.apkupdater.model.LogMessage;
import com.apkupdater.updater.UpdaterOptions;
import com.apkupdater.util.GooglePlayUtil;
import com.apkupdater.util.LogUtil;
import com.apkupdater.util.MyBus;
import com.apkupdater.util.SnackBarUtil;
import com.github.yeriomin.playstoreapi.DocV2;
import com.github.yeriomin.playstoreapi.GooglePlayAPI;
import com.github.yeriomin.playstoreapi.SearchIterator;
import com.github.yeriomin.playstoreapi.SearchResponse;

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

    @ViewById(R.id.input_search_layout)
    TextInputLayout mInputLayout;

    @ViewById(R.id.progress_bar)
    ProgressBar mProgressBar;

    @Bean
	MyBus mBus;

    @Bean
    LogUtil mLog;

    @Bean
    SearchAdapter mAdapter;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@AfterViews
	void init(
	) {
		mBus.register(this);

        // Set the input action listener
        mInputSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_NULL) {
                    search(mInputSearch.getText().toString());
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        mInputSearch.clearFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        // For smooth scrolling
        mRecyclerView.setNestedScrollingEnabled(false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onDestroy(
    ) {
		mBus.unregister(this);
		super.onDestroy();
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void search(
        final String text
    ) {
	    if (mProgressBar.getVisibility() == View.VISIBLE) {
	        return;
        }

        setListAdapter(new ArrayList<InstalledApp>());
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GooglePlayAPI api = GooglePlayUtil.getApi(getContext());

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
                } catch (RuntimeException rex) {
                    if (rex.getCause() != null) {
                        SnackBarUtil.make(getActivity(), String.valueOf(rex.getCause().getMessage()));
                    }
                    mLog.log("SearchFragment", String.valueOf(rex), LogMessage.SEVERITY_ERROR);
                    setListAdapter(new ArrayList<InstalledApp>());
                } catch (Exception e) {
                    SnackBarUtil.make(getActivity(), "Error searching.");
                    mLog.log("SearchFragment", String.valueOf(e), LogMessage.SEVERITY_ERROR);
                    setListAdapter(new ArrayList<InstalledApp>());
                }
            }
        }).start();
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@UiThread(propagation = UiThread.Propagation.REUSE)
	protected void setListAdapter(
		@NonNull List<InstalledApp> items
	) {
		if (mRecyclerView == null || mBus == null || mProgressBar == null) {
			return;
		}

		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (new UpdaterOptions(getContext()).disableAnimations()) {
            mRecyclerView.setItemAnimator(null);
        } else {
            ((SimpleItemAnimator) mRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        }
		mAdapter.init(getActivity(), mRecyclerView, items);
		mRecyclerView.setAdapter(mAdapter);
		mBus.post(new SearchTitleChange(getString(R.string.tab_search) + " (" + items.size() + ")"));
		mProgressBar.setVisibility(View.GONE);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
