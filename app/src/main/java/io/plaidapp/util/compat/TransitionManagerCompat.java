package io.plaidapp.util.compat;

import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.ViewGroup;

public final class TransitionManagerCompat {

    private TransitionManagerCompat() {
        throw new AssertionError("No instances.");
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(sceneRoot);
        }
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Transition transition) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            TransitionManager.beginDelayedTransition(sceneRoot, transition);
        }
    }
}
