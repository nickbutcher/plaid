/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.util;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with transitions
 */
public class TransitionUtils {

    private TransitionUtils() { }

    public static @Nullable Transition findTransition(
            @NonNull TransitionSet set, @NonNull Class<? extends Transition> clazz) {
        for (int i = 0; i < set.getTransitionCount(); i++) {
            Transition transition = set.getTransitionAt(i);
            if (transition.getClass() == clazz) {
                return transition;
            }
            if (transition instanceof TransitionSet) {
                Transition child = findTransition((TransitionSet) transition, clazz);
                if (child != null) return child;
            }
        }
        return null;
    }

    public static @Nullable Transition findTransition(
            @NonNull TransitionSet set,
            @NonNull Class<? extends Transition> clazz,
            @IdRes int targetId) {
        for (int i = 0; i < set.getTransitionCount(); i++) {
            Transition transition = set.getTransitionAt(i);
            if (transition.getClass() == clazz) {
                if (transition.getTargetIds().contains(targetId)) {
                    return transition;
                }
            }
            if (transition instanceof TransitionSet) {
                Transition child = findTransition((TransitionSet) transition, clazz, targetId);
                if (child != null) return child;
            }
        }
        return null;
    }

    public static List<Boolean> setAncestralClipping(@NonNull View view, boolean clipChildren) {
        return setAncestralClipping(view, clipChildren, new ArrayList<Boolean>());
    }

    private static List<Boolean> setAncestralClipping(
            @NonNull View view, boolean clipChildren, List<Boolean> was) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            was.add(group.getClipChildren());
            group.setClipChildren(clipChildren);
        }
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            setAncestralClipping((ViewGroup) parent, clipChildren, was);
        }
        return was;
    }

    public static void restoreAncestralClipping(@NonNull View view, List<Boolean> was) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            group.setClipChildren(was.remove(0));
        }
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            restoreAncestralClipping((ViewGroup) parent, was);
        }
    }

    public static class TransitionListenerAdapter implements Transition.TransitionListener {

        @Override public void onTransitionStart(Transition transition) { }

        @Override public void onTransitionEnd(Transition transition) { }

        @Override public void onTransitionCancel(Transition transition) { }

        @Override public void onTransitionPause(Transition transition) { }

        @Override public void onTransitionResume(Transition transition) { }
    }
}
