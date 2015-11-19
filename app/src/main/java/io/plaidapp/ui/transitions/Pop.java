/*
 * Copyright 2015 Google Inc.
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

package io.plaidapp.ui.transitions;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import io.plaidapp.util.AnimUtils;

/**
 * A transition that animates the alpha & scale X & Y of a view simultaneously.
 */
public class Pop extends Visibility {

    private static final String PROPNAME_ALPHA = "plaid:pop:alpha";
    private static final String PROPNAME_SCALE_X = "plaid:pop:scaleX";
    private static final String PROPNAME_SCALE_Y = "plaid:pop:scaleY";

    private static final String[] transitionProperties = {
            PROPNAME_ALPHA,
            PROPNAME_SCALE_X,
            PROPNAME_SCALE_Y
    };

    public Pop(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public String[] getTransitionProperties() {
        return transitionProperties;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        transitionValues.values.put(PROPNAME_ALPHA, 0f);
        transitionValues.values.put(PROPNAME_SCALE_X, 0f);
        transitionValues.values.put(PROPNAME_SCALE_Y, 0f);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        transitionValues.values.put(PROPNAME_ALPHA, 1f);
        transitionValues.values.put(PROPNAME_SCALE_X, 1f);
        transitionValues.values.put(PROPNAME_SCALE_Y, 1f);
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view, TransitionValues startValues,
                             TransitionValues endValues) {
        return new AnimUtils.NoPauseAnimator(ObjectAnimator.ofPropertyValuesHolder(
                endValues.view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f)));
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view, TransitionValues startValues,
                                TransitionValues endValues) {
        return new AnimUtils.NoPauseAnimator(ObjectAnimator.ofPropertyValuesHolder(
                endValues.view,
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f)));
    }

}
