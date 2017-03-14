package com.apkupdater.util;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

import android.os.Build;
import android.support.transition.AutoTransition;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.ViewGroup;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

public class AnimationUtil
{
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startListAnimation(
        ViewGroup v
    ) {
        if (Build.VERSION.SDK_INT >= 21) {
            TransitionManager.beginDelayedTransition(v, new Slide());
        } else if (Build.VERSION.SDK_INT >= 14){
            android.support.transition.TransitionManager.beginDelayedTransition(v);
        } {
            // No animation
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void startToolbarAnimation(
        ViewGroup v
    ) {
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
