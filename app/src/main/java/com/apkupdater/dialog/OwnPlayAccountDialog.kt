package com.apkupdater.dialog

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.ContextThemeWrapper
import android.view.View
import com.apkupdater.R
import com.apkupdater.model.Constants
import com.apkupdater.updater.UpdaterOptions
import com.apkupdater.util.DownloadUtil
import com.apkupdater.util.GooglePlayUtil
import com.apkupdater.util.ThemeUtil
import com.github.yeriomin.playstoreapi.AuthException
import kotlinx.android.synthetic.main.dialog_own_play.view.*
import kotlin.concurrent.thread

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class OwnPlayAccountDialog
    : DialogFragment()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    companion object {
        const val ResultSuccess = 0
        const val ResultFailure = 1
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private var mView : View? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        return AlertDialog.Builder(ContextThemeWrapper(context, ThemeUtil.getActivityThemeFromOptions(context)))
            .setTitle("Setup Play Account")
            .setView(getContentView())
            .setNegativeButton(getString(R.string.get_token_cancel), null)
            .setPositiveButton(getString(R.string.get_token_get_token), null)
            .setNeutralButton(getString(R.string.get_token_help), null)
            .setCancelable(false)
            .create()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onStart(
    ) {
        super.onStart()

        // Override click handlers
        val d = dialog as AlertDialog
        d.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener(this::onNegativeButtonClick)
        d.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(this::onPositiveButtonClick)
        d.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener(this::onNeutralButton)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("UNUSED_PARAMETER")
    private fun onNeutralButton(
        view : View
    ) {
        DownloadUtil.launchBrowser(context, Constants.OwnAccountHelpURL)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("UNUSED_PARAMETER")
    private fun onPositiveButtonClick(
        view : View
    ) {
        thread {
            try {
                mView?.post {
                    mView?.error_layout?.visibility = View.VISIBLE
                    mView?.progress_bar?.visibility = View.VISIBLE
                    mView?.error_text?.text = ""
                }

                val pair = GooglePlayUtil.getIdTokenPairFromEmailPassword(
                    context,
                    mView?.user_edit_text?.text.toString(),
                    mView?.password_edit_text?.text.toString()
                )

                val options: UpdaterOptions = UpdaterOptions(context)

                if (!pair.first.isNullOrEmpty() && !pair.second.isNullOrEmpty()) {
                    options.ownGsfId = pair.first
                    options.ownToken = pair.second
                    targetFragment.onActivityResult(Constants.OwnPlayAccountRequestCode, ResultSuccess, null)
                    dismiss()
                } else {
                    throw Exception("No gsfid or token.")
                }
            } catch (ex: AuthException) {
                mView?.post {
                    mView?.progress_bar?.visibility = View.GONE
                    mView?.error_text?.text = context.getString(R.string.get_token_launch_url)
                }
                mView?.postDelayed( {DownloadUtil.launchBrowser(context, ex.twoFactorUrl) }, 1000)
            } catch (e : Exception) {
                mView?.post {
                    mView?.progress_bar?.visibility = View.GONE
                    mView?.error_text?.text = context.getString(R.string.get_token_error)
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Suppress("UNUSED_PARAMETER")
    private fun onNegativeButtonClick(
        view : View
    ) {
        targetFragment.onActivityResult(Constants.OwnPlayAccountRequestCode, ResultFailure, null)
        dismiss()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private fun getContentView(
    ) : View
    {
        mView = activity.layoutInflater?.inflate(R.layout.dialog_own_play, null, false)
        return mView as View
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////