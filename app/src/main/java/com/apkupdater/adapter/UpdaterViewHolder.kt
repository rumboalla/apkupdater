package com.apkupdater.adapter

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.view.ViewGroup
import com.apkupdater.R
import com.apkupdater.activity.MainActivity
import com.apkupdater.model.*
import com.apkupdater.util.*
import com.github.yeriomin.playstoreapi.GooglePlayException
import kotlinx.android.synthetic.main.updater_item.view.*
import uy.kohesive.injekt.api.get
import uy.kohesive.injekt.injectValue
import kotlin.concurrent.thread

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

open class UpdaterViewHolder(view: View)
	: RecyclerView.ViewHolder(view)
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected var mView : View? = view
	protected var mContext : Context? = view.context
	protected val mLog : LogUtil = InjektUtil.injekt?.get()!!
	protected val mBus: MyBus = InjektUtil.injekt?.get()!!
	protected val mActivity : MainActivity = InjektUtil.injekt?.get()!!
	protected val mAppState : AppState = InjektUtil.injekt?.get()!!

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	open fun bind(
		adapter : UpdaterAdapter,
		u : Update?
	) {
		mView?.installed_app_name?.text = u?.name
		mView?.installed_app_pname?.text = u?.pname
		mView?.installed_app_version?.text =
			String.format("%1s (%2s) -> %3s (4%s)", u?.version, u?.versionCode, u?.newVersion, u?.newVersionCode)

		// Icon
		mView?.installed_app_icon?.setImageDrawable(mView?.context?.packageManager?.getApplicationIcon(u?.pname))

		// Beta icon
		mView?.isbeta_icon?.visibility = if (u?.isBeta as Boolean) View.VISIBLE else View.GONE
		mView?.isbeta_icon?.background?.setColorFilter(
			ColorUtil.getColorFromTheme(mContext?.theme, R.attr.colorAccent),
			android.graphics.PorterDuff.Mode.MULTIPLY
		)

		// Click handler for expand/collapse
		mView?.setOnClickListener{
			AnimationUtil.startDefaultAnimation(mContext, mView?.change_log_container)
			mView?.change_log_container?.visibility =
				if (mView?.change_log_container?.visibility == View.GONE) View.VISIBLE else View.GONE
		}

		// Changelog
		if (u.changeLog?.isNullOrEmpty() ?: true) {
			mView?.change_log_text?.text = ""
			mView?.change_log_text?.visibility = View.GONE
		} else {
			mView?.change_log_text?.text = Html.fromHtml(u.changeLog)
			mView?.change_log_text?.visibility = View.VISIBLE
		}

		configureActionButton(u)
		setTopMargin(0)
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun configureActionButton(
		u : Update
	) {
		val action : String = getActionString(u)
		mView?.action_one_button?.text = action
		mView?.action_one_button?.visibility = View.VISIBLE
		mView?.action_one_progressbar?.visibility = View.INVISIBLE

		if (action == mContext?.getString(R.string.action_play)) {
			if (u.installStatus.status == InstallStatus.STATUS_INSTALL) {
				mView?.action_one_button?.text = getActionString(u)
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLED) {
				mView?.action_one_button?.setText(R.string.action_installed)
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLING) {
				mView?.action_one_progressbar?.visibility = View.VISIBLE
				mView?.action_one_button?.visibility = View.INVISIBLE
			}
		}

		// Choose action depending if it's INSTALL or other
		mView?.action_one_button?.setOnClickListener { if (action == mContext?.getString(R.string.action_play)) launchInstall(u) else launchBrowser(u) }
		mView?.action_two_button?.visibility = View.GONE
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun launchInstall(
		u : Update
	) {
		changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALLING, 0)
		thread {
			try {
				val data = GooglePlayUtil.getAppDeliveryData(GooglePlayUtil.getApi(mContext), u.pname)

				val id = DownloadUtil.downloadFile(
					mContext,
					data.downloadUrl,
					data.getDownloadAuthCookie(0).name + "=" + data.getDownloadAuthCookie(0).value,
					u.pname + " " + u.newVersionCode
				)

				mAppState.downloadInfo.put(id, DownloadInfo(u.pname, u.newVersionCode, u.newVersion))
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALLING, id)
			} catch (gex: GooglePlayException) {
				SnackBarUtil.make(mActivity, gex.message.toString())
				mLog.log("UpdaterAdapter", gex.toString(), LogMessage.SEVERITY_ERROR)
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALL, 0)
			} catch (e: Exception) {
				SnackBarUtil.make(mActivity, "Error downloading.")
				mLog.log("UpdaterAdapter", e.toString(), LogMessage.SEVERITY_ERROR)
				changeAppInstallStatusAndNotify(u, InstallStatus.STATUS_INSTALL, 0)
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun launchBrowser(
		u : Update
	) {
		DownloadUtil.launchBrowser(mContext, u.url)
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun getActionString(
		update : Update
	) : String {
		if (update.url.contains("apkmirror.com")) {
			return mContext?.getString(R.string.action_apkmirror)!!
		} else if (update.url.contains("uptodown.com")) {
			return mContext?.getString(R.string.action_uptodown)!!
		} else if (update.url.contains("apkpure.com")) {
			return mContext?.getString(R.string.action_apkpure)!!
		} else if (update.cookie != null) {
			return mContext?.getString(R.string.action_play)!!
		}
		return "ERROR"
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun changeAppInstallStatusAndNotify(
		app: Update?,
		status: Int,
		id: Long
	) {
		val adapter : UpdaterAdapter = InjektUtil.injekt?.get()!!
		app?.installStatus?.id = id
		app?.installStatus?.status = status
		mView?.post {
			adapter.notifyItemChanged(adapterPosition)
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	fun setTopMargin(
		margin: Int
	) {
		val params = mView?.layoutParams as ViewGroup.MarginLayoutParams?
		params?.topMargin = PixelConversion.convertDpToPixel(margin.toFloat(), mContext).toInt()
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////