package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.content.Context;
import android.os.Build;
import android.support.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.ViewGroup;

import com.apkupdater.updater.UpdaterOptions;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class AnimationUtil
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startDefaultAnimation(
        Context context,
        ViewGroup v
    ) {
        if (new UpdaterOptions(context).disableAnimations()) {
            // No animation
        } else if (Build.VERSION.SDK_INT >= 21) {
            TransitionManager.beginDelayedTransition(v);
        } else if (Build.VERSION.SDK_INT >= 14){
            android.support.transition.TransitionManager.beginDelayedTransition(v);
        } {
            // No animation
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startSlideAnimation(
        Context context,
        ViewGroup v
    ) {
        if (new UpdaterOptions(context).disableAnimations()) {
            // No animation
        } else if (Build.VERSION.SDK_INT >= 21) {
            TransitionManager.beginDelayedTransition(v, new Slide());
        } else if (Build.VERSION.SDK_INT >= 14){
            android.support.transition.TransitionManager.beginDelayedTransition(v); // Support v26 will add Slide
        } {
            // No animation
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startToolbarAnimation(
        Context context,
        ViewGroup v
    ) {
        if (new UpdaterOptions(context).disableAnimations()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 21) {
            TransitionManager.beginDelayedTransition(v, new TransitionSet()
                .addTransition(new Fade(Fade.IN))
                .addTransition(new Slide(Gravity.LEFT))
                .addTransition(new ChangeBounds())
            );

        } else if (Build.VERSION.SDK_INT >= 14){
            android.support.transition.TransitionManager.beginDelayedTransition(v, new AutoTransition().setDuration(250));
        } {
            // No animation
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
