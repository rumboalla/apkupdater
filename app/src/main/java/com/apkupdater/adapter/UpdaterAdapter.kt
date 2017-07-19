package com.apkupdater.adapter

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.apkupdater.R
import com.apkupdater.event.InstallAppEvent
import com.apkupdater.model.Update
import com.apkupdater.util.MyBus
import com.squareup.otto.Subscribe
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import com.apkupdater.util.DownloadUtil
import com.apkupdater.event.SnackBarEvent
import com.apkupdater.model.InstallStatus
import com.apkupdater.util.InjektUtil
import uy.kohesive.injekt.api.get

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class UpdaterAdapter
    : RecyclerView.Adapter<UpdaterViewHolder>
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private var mUpdates: MutableList<Update>? = null
    private var mContext: Context? = null
    private var mView: RecyclerView? = null
	private val mBus: MyBus = InjektUtil.injekt?.get()!!

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    constructor(
        context : Context
    ) {
        mContext = context
        InjektUtil.addUpdaterAdapterSingleton(this)
        mBus.register(this)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun init(
        view: RecyclerView,
        updates: MutableList<Update>
    ) {
        mView = view
        setUpdates(updates)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun setUpdates(
        updates: MutableList<Update>
    ) {
        mUpdates = updates
        sort()
        notifyDataSetChanged()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun sort(
    ) {
        mUpdates = mUpdates?.sortedWith(compareBy(Update::isBeta).thenBy(Update::getName))?.toMutableList()
        mView?.layoutManager?.scrollToPosition(0)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun addUpdate(
        update: Update
    ) {
        mUpdates?.add(update)
        sort()
        val index = mUpdates?.indexOf(update)
        notifyItemChanged(0)
        notifyItemInserted(index!!)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun getItemCount(
    ): Int {
        return mUpdates?.size ?: 0
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	fun getCount(
	): Int {
		return itemCount
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onBindViewHolder(
        holder: UpdaterViewHolder?,
        position: Int
    ) {
        holder?.bind(this, mUpdates?.get(position))

        if (position == 0) {
            holder?.setTopMargin(8)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateViewHolder(
        parent: ViewGroup?,
        viewType: Int
    ): UpdaterViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.updater_item, parent, false)
        return UpdaterViewHolder(v)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Subscribe
    fun onInstallAppEven(
        ev : InstallAppEvent
    ) {
	    mUpdates?.forEachIndexed { i, app ->
		    if (app.installStatus.id == ev.id) {
			    app.installStatus.id = 0
			    if (ev.isSuccess) {
				    app.installStatus.status = InstallStatus.STATUS_INSTALLED
				    mBus.post(SnackBarEvent(mContext?.getString(R.string.install_success)))
			    } else {
				    // If the app is already set as installed, do nothing
				    if (app.installStatus.status == InstallStatus.STATUS_INSTALLING) {
					    app.installStatus.status = InstallStatus.STATUS_INSTALL
					    mBus.post(SnackBarEvent(mContext?.getString(R.string.install_failure)))
				    }
			    }
			    notifyItemChanged(i)
			    DownloadUtil.deleteDownloadedFile(mContext!!, app.installStatus.id)
		    }
	    }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////