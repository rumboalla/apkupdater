package de.apkgrabber.adapter

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.view.ViewGroup
import de.apkgrabber.R
import de.apkgrabber.activity.MainActivity
import de.apkgrabber.model.*
import de.apkgrabber.util.*
import com.github.yeriomin.playstoreapi.GooglePlayException
import kotlinx.android.synthetic.main.updater_item.view.*
import kotlin.concurrent.thread
import android.support.v7.widget.LinearLayoutManager
import de.apkgrabber.updater.UpdaterOptions
import uy.kohesive.injekt.api.get

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
		updates : MergedUpdate
	) {
		val u : Update = updates.updateList[0]

		mView?.installed_app_name?.text = u.name
		mView?.installed_app_pname?.text = u.pname

		val v = if(u.newVersion == "?" && updates.updateList.size > 1) updates.updateList[1].newVersion else u.newVersion

		mView?.installed_app_version?.text =
			String.format("%s (%s) -> %s (%s)", u.version, u.versionCode, v, u.newVersionCode)

		// Icon
		mView?.installed_app_icon?.setImageDrawable(mView?.context?.packageManager?.getApplicationIcon(u.pname))

		// Beta icon
		mView?.isbeta_icon?.visibility = if (u.isBeta) View.VISIBLE else View.GONE
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

		mView?.button_bar?.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false)
		mView?.button_bar?.adapter = ButtonBarAdapter(mContext as Context)

		updates.updateList.forEach { configureActionButton(it) }
		configureIgnoreButton(u)
		setTopMargin(0)
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun configureActionButton(
		u : Update
	) {
		val text : String = getActionString(u)
		val adapter : ButtonBarAdapter = mView?.button_bar?.adapter as ButtonBarAdapter
		adapter.addButton(ActionButton(
			text,
			u.installStatus.status == InstallStatus.STATUS_INSTALLING,
            {
                if (text == mContext?.getString(R.string.action_play)) {
					launchInstall(u)
				} else if(text == mContext?.getString(R.string.action_aptoide)) {
					val splits = u.url.split("/")
					directDownload(u, splits.last())
				} else {
					launchBrowser(u)
				}
            }
		))
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun configureIgnoreButton(
		u : Update
	) {
		val adapter : ButtonBarAdapter = mView?.button_bar?.adapter as ButtonBarAdapter
		adapter.addButton(ActionButton(
			mContext?.getString(R.string.action_ignore_app)!!,
			false,
			{
				val options : UpdaterOptions = UpdaterOptions(mContext)
				val l = options.ignoreVersionList
				l.add(IgnoreVersion(u.pname, u.newVersion, u.newVersionCode))
				options.ignoreVersionList = l
				val adapter : UpdaterAdapter = InjektUtil.injekt?.get()!!
				adapter.removeUpdate(u)
				adapter.notifyDataSetChanged()
			}
		))
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

	private fun directDownload(u : Update, name : String) {
		DownloadUtil.downloadFile(mContext, u.url, "", name)
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private fun getActionString(
		u : Update
	) : String {
		if (u.url.contains("apkmirror.com")) {
			return mContext?.getString(R.string.action_apkmirror)!!
		} else if (u.url.contains("uptodown.com")) {
			return mContext?.getString(R.string.action_uptodown)!!
		} else if (u.url.contains("apkpure.com")) {
			return mContext?.getString(R.string.action_apkpure)!!
		} else if (u.url.contains("aptoide.com")) {
			return mContext?.getString(R.string.action_aptoide)!!
		} else if (u.cookie != null) {
			if (u.installStatus.status == InstallStatus.STATUS_INSTALL) {
				return mContext?.getString(R.string.action_play)!!
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLED) {
				return mContext?.getString(R.string.action_installed)!!
			} else if (u.installStatus.status == InstallStatus.STATUS_INSTALLING) {
				return ""
			}
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