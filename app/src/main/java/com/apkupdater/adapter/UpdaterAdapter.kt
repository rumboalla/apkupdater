package com.apkupdater.adapter

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.apkupdater.R
import com.apkupdater.event.InstallAppEvent
import com.apkupdater.event.RefreshUpdateTitle
import com.apkupdater.model.Update
import com.apkupdater.util.MyBus
import com.squareup.otto.Subscribe
import com.apkupdater.util.DownloadUtil
import com.apkupdater.event.SnackBarEvent
import com.apkupdater.model.IgnoreVersion
import com.apkupdater.model.InstallStatus
import com.apkupdater.model.MergedUpdate
import com.apkupdater.updater.UpdaterOptions
import com.apkupdater.util.InjektUtil
import uy.kohesive.injekt.api.get

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class UpdaterAdapter
    : RecyclerView.Adapter<UpdaterViewHolder>
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private var mUpdates : MutableList<Update>? = null
    private var mContext : Context? = null
    private var mView : RecyclerView? = null
	private val mBus : MyBus = InjektUtil.injekt?.get()!!
	private var mMergedUpdates : MutableList<MergedUpdate> = mutableListOf()

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
        sort(true)
        notifyDataSetChanged()
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	fun mergeUpdates(
		updates: MutableList<Update>
	) : MutableList<MergedUpdate> {
		val mergedUpdates : MutableList<MergedUpdate> = mutableListOf()
		val skip : MutableList<Pair<String, Int>> = mutableListOf()
		updates.forEach {
			if (!skip.contains(Pair(it.pname, it.newVersionCode))) {
				mergedUpdates.add(MergedUpdate(updates.filter {
					cit -> it.newVersionCode == cit.newVersionCode && it.pname == cit.pname
				}))
				skip.add(Pair(it.pname, it.newVersionCode))
			}
		}
		return mergedUpdates
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun sort(
	    b : Boolean
    ) {
	    // Filter and sort updates
	    mUpdates = sortUpdates(mContext as Context, mUpdates!!)
	    mMergedUpdates = mergeUpdates(mUpdates!!)

	    // Make sure we go to the start of the list
        if (b) {
	        mView?.layoutManager?.scrollToPosition(0)
        }
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	companion object {
		fun sortUpdates(
			context: Context,
			updates: MutableList<Update>
		) : MutableList<Update> {
			val l = UpdaterOptions(context).ignoreVersionList
			return updates?.filter {
				val ignore = IgnoreVersion(it.pname, it.newVersion, it.newVersionCode)
				l.find {
					it.packageName == ignore.packageName
					&& it.versionName == ignore.versionName
					&& it.versionCode == ignore.versionCode
				} == null
			}?.sortedWith(compareBy(Update::isBeta).thenBy(Update::getName))?.toMutableList()
		}
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun addUpdate(
        update: Update
    ) {
        mUpdates?.add(update)
        sort(true)
        val index = mUpdates?.indexOf(update) as Int
	    notifyItemChanged(0)
        if (index >= 0) {
	        getIndexForUpdate(update, {index -> notifyItemInserted(index)})
        }
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	fun removeUpdate(
		u : Update
	) {
		try {
			val i = mUpdates?.indexOf(u) as Int
			mUpdates?.removeAt(i)
			sort(false)
			getIndexForUpdate(u, {index -> notifyItemRemoved(index)})
			mBus.post(RefreshUpdateTitle())
		} catch (e : Exception) {}
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun getItemCount(
    ): Int {
        return mMergedUpdates.size
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onBindViewHolder(
        holder: UpdaterViewHolder?,
        position: Int
    ) {
        holder?.bind(this, mMergedUpdates[position])

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

	fun getIndexForUpdate(
		u : Update,
		callback : (Int) -> Unit
	) {
		mMergedUpdates.forEachIndexed { i, it ->
			if (it.updateList.contains(u)) {
				callback(i)
				return
			}
		}
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
			    getIndexForUpdate(app, {index -> notifyItemChanged(index)})
			    DownloadUtil.deleteDownloadedFile(mContext!!, app.installStatus.id)
		    }
	    }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////