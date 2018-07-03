/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.plaidapp.core.R;

/**
 * A transition which sets a specified {@link Animatable} {@code drawable} on a target
 * {@link ImageView} and {@link Animatable#start() starts} it when the transition begins.
 */
public class StartAnimatable extends Transition {

    private final Animatable animatable;

    public StartAnimatable(Animatable animatable) {
        super();
        if (!(animatable instanceof Drawable)) {
            throw new IllegalArgumentException("Non-Drawable resource provided.");
        }
        this.animatable = animatable;
    }

    public StartAnimatable(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StartAnimatable);
        Drawable drawable = a.getDrawable(R.styleable.StartAnimatable_android_src);
        a.recycle();
        if (drawable instanceof Animatable) {
            animatable = (Animatable) drawable;
        } else {
            throw new IllegalArgumentException("Non-Animatable resource provided.");
        }
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        // no-op
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        // no-op
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues) {
        if (animatable == null || endValues == null
                || !(endValues.view instanceof ImageView)) return null;

        ImageView iv = (ImageView) endValues.view;
        iv.setImageDrawable((Drawable) animatable);

        // need to return a non-null Animator even though we just want to listen for the start
        ValueAnimator transition = ValueAnimator.ofInt(0, 1);
        transition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                animatable.start();
            }
        });
        return transition;
    }
}
