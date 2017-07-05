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
import com.apkupdater.util.GooglePlayUtil
import com.apkupdater.util.ThemeUtil
import kotlinx.android.synthetic.main.dialog_own_play.view.*
import kotlin.concurrent.thread

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class OwnPlayAccountDialog
    : DialogFragment()
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    val ResultSuccess : Int = 0
    val ResultFailure : Int = 1

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    var mView : View? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onCreateDialog(
        savedInstanceState: Bundle?
    ): Dialog {
        return AlertDialog.Builder(ContextThemeWrapper(context, ThemeUtil.getActivityThemeFromOptions(context)))
            .setTitle("Setup Play Account")
            .setView(getContentView())
            .setNegativeButton("CANCEL", { _, _ ->  })
            .setPositiveButton("GET TOKEN", { _, _ -> })
            .create()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    override fun onStart(

    ) {
        super.onStart()

        val d = dialog as AlertDialog

        // Override negative button click handler
        d.getButton(Dialog.BUTTON_NEGATIVE).setOnClickListener({
            targetFragment.onActivityResult(Constants.OwnPlayAccountRequestCode, ResultFailure, null)
            dismiss()
        })

        // Override positive button click handler
        d.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener({
            val v = mView as View
            v.error_text?.text = ""

            thread(start = true) {
                try {
                    val t = GooglePlayUtil.getIdTokenPairFromEmailPassword(
                        context,
                        v.user_edit_text?.text.toString(),
                        v.password_edit_text?.text.toString()
                    )

                    t.first

                    v.post {
                        targetFragment.onActivityResult(Constants.OwnPlayAccountRequestCode, ResultSuccess, null)
                        dismiss()
                    }
                } catch (e : Exception) {
                    v.post {
                        v.error_text?.text = "There was an error attempting to get the token. Two factor auth is not yet supported."
                    }
                }
            }
        })

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun getContentView(
    ) : View
    {
        mView = activity.layoutInflater?.inflate(R.layout.dialog_own_play, null, false)
        return mView as View
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////