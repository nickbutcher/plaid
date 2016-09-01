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

package io.plaidapp.ui.transitions;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import io.plaidapp.util.ViewUtils;

/**
 * A transition which fades in/out the background {@link Drawable} of a View.
 */
public class BackgroundFade extends Visibility {

    public BackgroundFade() {
        super();
    }

    @Keep
    public BackgroundFade(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
                             TransitionValues startValues, TransitionValues endValues) {
        if (view == null || view.getBackground() == null) return null;
        Drawable background = view.getBackground();
        background.setAlpha(0);
        return ObjectAnimator.ofInt(background, ViewUtils.DRAWABLE_ALPHA, 0, 255);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view,
                                TransitionValues startValues, TransitionValues endValues) {
        if (view == null || view.getBackground() == null) return null;
        return ObjectAnimator.ofInt(view.getBackground(), ViewUtils.DRAWABLE_ALPHA, 0);
    }
}
