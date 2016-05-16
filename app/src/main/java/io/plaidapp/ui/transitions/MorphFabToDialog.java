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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import io.plaidapp.R;
import io.plaidapp.ui.drawable.MorphDrawable;
import io.plaidapp.util.AnimUtils;

/**
 * A transition that morphs a circle into a rectangle, changing it's background color.
 */
public class MorphFabToDialog extends ChangeBounds {

    private static final String PROPNAME_COLOR = "plaid:fabMorph:color";
    private static final String PROPNAME_BOUNDS = "plaid:fabMorph:bounds";
    private static final String PROBNAME_CORNER_RADIUS = "plaid:fabMorph:cornerRadius";
    private static final String[] TRANSITION_PROPERTIES = {
            PROPNAME_COLOR,
            PROPNAME_BOUNDS,
            PROBNAME_CORNER_RADIUS
    };
    private @ColorInt int startColor = Color.TRANSPARENT;
    private int endCornerRadius;
    private int startCornerRadius;

    public MorphFabToDialog(@ColorInt int startColor, int endCornerRadius) {
        this(startColor, endCornerRadius, -1);
    }

    public MorphFabToDialog(@ColorInt int startColor, int endCornerRadius, int startCornerRadius) {
        super();
        setStartColor(startColor);
        setEndCornerRadius(endCornerRadius);
        setStartCornerRadius(startCornerRadius);
    }

    public MorphFabToDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStartColor(@ColorInt int startColor) {
        this.startColor = startColor;
    }

    public void setEndCornerRadius(int endCornerRadius) {
        this.endCornerRadius = endCornerRadius;
    }

    public void setStartCornerRadius(int startCornerRadius) {
        this.startCornerRadius = startCornerRadius;
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPNAME_COLOR, startColor);
        transitionValues.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(),
                view.getRight(), view.getBottom()));
        transitionValues.values.put(PROBNAME_CORNER_RADIUS,
                startCornerRadius >= 0 ? startCornerRadius : view.getHeight() / 2);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(PROPNAME_COLOR,
                ContextCompat.getColor(view.getContext(), R.color.background_light));
        transitionValues.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(),
                view.getRight(), view.getBottom()));
        transitionValues.values.put(PROBNAME_CORNER_RADIUS, endCornerRadius);
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   final TransitionValues startValues,
                                   final TransitionValues endValues) {
        final Animator changeBounds = super.createAnimator(sceneRoot, startValues, endValues);
        if (startValues == null || endValues == null || changeBounds == null) {
            return null;
        }

        final Integer startColor = (Integer) startValues.values.get(PROPNAME_COLOR);
        final Integer startCornerRadius = (Integer) startValues.values.get(PROBNAME_CORNER_RADIUS);
        final Integer endColor = (Integer) endValues.values.get(PROPNAME_COLOR);
        final Integer endCornerRadius = (Integer) endValues.values.get(PROBNAME_CORNER_RADIUS);

        if (startColor == null || startCornerRadius == null || endColor == null ||
                endCornerRadius == null) {
            return null;
        }

        final MorphDrawable background = new MorphDrawable(startColor, startCornerRadius);
        endValues.view.setBackground(background);

        final Animator color = ObjectAnimator.ofArgb(background, background.COLOR, endColor);
        final Animator corners = ObjectAnimator.ofFloat(background, background.CORNER_RADIUS,
                endCornerRadius);

        // ease in the dialog's child views (slide up & fade in)
        if (endValues.view instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) endValues.view;
            float offset = vg.getHeight() / 3;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setTranslationY(offset);
                v.setAlpha(0f);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(150)
                        .setStartDelay(150)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(vg.getContext()));
                offset *= 1.8f;
            }
        }

        final AnimatorSet transition = new AnimatorSet();
        transition.playTogether(changeBounds, corners, color);
        transition.setDuration(300);
        transition.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(sceneRoot.getContext()));
        return transition;
    }

}
