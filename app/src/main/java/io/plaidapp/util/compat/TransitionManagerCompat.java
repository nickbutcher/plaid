package io.plaidapp.util.compat;

import android.view.ViewGroup;

import com.transitionseverywhere.Transition;
import com.transitionseverywhere.TransitionManager;

public final class TransitionManagerCompat {

    private TransitionManagerCompat() {
        throw new AssertionError("No instances.");
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot) {
        TransitionManager.beginDelayedTransition(sceneRoot);
    }

    public static void beginDelayedTransition(ViewGroup sceneRoot, Transition transition) {
        TransitionManager.beginDelayedTransition(sceneRoot, transition);
    }
}
