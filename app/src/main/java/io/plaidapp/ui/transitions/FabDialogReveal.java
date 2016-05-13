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
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.R;
import io.plaidapp.util.AnimUtils;

import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * A transition between a FAB & a dialog using a circular reveal and following an arced path.
 */
public class FabDialogReveal extends Transition {

    private static final String EXTRA_FAB_COLOR = "EXTRA_FAB_COLOR";
    private static final String EXTRA_FAB_ICON_RES_ID = "EXTRA_FAB_ICON_RES_ID";
    private static final String PROP_BOUNDS = "plaid:fabDialogReveal:bounds";
    private static final String[] TRANSITION_PROPERTIES = {
            PROP_BOUNDS
    };

    private final int color;
    private final int icon;

    public FabDialogReveal(@ColorInt int fabColor, @DrawableRes int iconResId) {
        color = fabColor;
        icon = iconResId;
        setPathMotion(new GravityArcMotion());
    }

    public FabDialogReveal(Context context, AttributeSet attrs) {
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(attrs, R.styleable.FabDialogReveal);
            if (!a.hasValue(R.styleable.FabDialogReveal_android_color)
                    || !a.hasValue(R.styleable.FabDialogReveal_android_icon)) {
                throw new IllegalArgumentException("Must provide both color & icon.");
            }
            color = a.getColor(R.styleable.FabDialogReveal_android_color, Color.TRANSPARENT);
            icon = a.getResourceId(R.styleable.FabDialogReveal_android_icon, 0);
            setPathMotion(new GravityArcMotion());
        } finally {
            a.recycle();
        }
    }

    public static void addExtras(@NonNull Intent intent, @ColorInt int fabColor,
                                 @DrawableRes int iconResId) {
        intent.putExtra(FabDialogReveal.EXTRA_FAB_COLOR, fabColor);
        intent.putExtra(FabDialogReveal.EXTRA_FAB_ICON_RES_ID, iconResId);
    }

    public static void setup(@NonNull Activity activity, @Nullable View target) {
        final Intent intent = activity.getIntent();
        if (!intent.hasExtra(EXTRA_FAB_COLOR) || !intent.hasExtra(EXTRA_FAB_ICON_RES_ID)) return;
        final int color = intent.
                getIntExtra(EXTRA_FAB_COLOR, Color.TRANSPARENT);
        final int icon = intent.getIntExtra(EXTRA_FAB_ICON_RES_ID, -1);
        final FabDialogReveal sharedEnter = new FabDialogReveal(color, icon);
        if (target != null) {
            sharedEnter.addTarget(target);
        }
        activity.getWindow().setSharedElementEnterTransition(sharedEnter);
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   final TransitionValues endValues) {
        if (startValues == null || endValues == null)  return null;

        final Rect startBounds = (Rect) startValues.values.get(PROP_BOUNDS);
        final Rect endBounds = (Rect) endValues.values.get(PROP_BOUNDS);

        final boolean fabToDialog = endBounds.width() > startBounds.width();
        final View view = endValues.view;
        final Rect dialogBounds = fabToDialog ? endBounds : startBounds;
        final Rect fabBounds = fabToDialog ? startBounds : endBounds;
        final Interpolator fastOutSlowInInterpolator =
                AnimUtils.getFastOutSlowInInterpolator(sceneRoot.getContext());

        if (!fabToDialog) {
            // force measure / layout the dialog back to it's orig bounds
            view.measure(
                    makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY),
                    makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY));
            view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom);
        }

        final int translationX = startBounds.centerX() - endBounds.centerX();
        final int translationY = startBounds.centerY() - endBounds.centerY();
        if (fabToDialog) {
            view.setTranslationX(translationX);
            view.setTranslationY(translationY);
        }

        final ColorDrawable fabColor = new ColorDrawable(color);
        fabColor.setBounds(0, 0, dialogBounds.width(), dialogBounds.height());
        if (!fabToDialog) fabColor.setAlpha(0);
        view.getOverlay().add(fabColor);

        final Drawable fabIcon =
                ContextCompat.getDrawable(sceneRoot.getContext(), icon).mutate();
        final int iconLeft = (dialogBounds.width() - fabIcon.getIntrinsicWidth()) / 2;
        final int iconTop = (dialogBounds.height() - fabIcon.getIntrinsicHeight()) / 2;
        fabIcon.setBounds(iconLeft, iconTop,
                iconLeft + fabIcon.getIntrinsicWidth(),
                iconTop + fabIcon.getIntrinsicHeight());
        if (!fabToDialog) fabIcon.setAlpha(0);
        view.getOverlay().add(fabIcon);

        final Animator circularReveal;
        if (fabToDialog) {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    (view.getRight() - view.getLeft()) / 2,
                    (view.getBottom() - view.getTop()) / 2,
                    startBounds.width() / 2,
                    (float) Math.hypot(endBounds.width() / 2, endBounds.width() / 2));
            circularReveal.setInterpolator(
                    AnimUtils.getFastOutLinearInInterpolator(sceneRoot.getContext()));
        } else {
            circularReveal = ViewAnimationUtils.createCircularReveal(view,
                    (view.getRight() - view.getLeft()) / 2,
                    (view.getBottom() - view.getTop()) / 2,
                    (float) Math.hypot(startBounds.width() / 2, startBounds.width() / 2),
                    endBounds.width() / 2);
            circularReveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setOutlineProvider(new ViewOutlineProvider() {
                        @Override
                        public void getOutline(View view, Outline outline) {
                            final int left = (view.getWidth() - fabBounds.width()) / 2;
                            final int top = (view.getHeight() - fabBounds.height()) / 2;
                            outline.setOval(
                                    left, top, left + fabBounds.width(), top + fabBounds.height());
                            view.setClipToOutline(true);
                        }
                    });
                }
            });
            circularReveal.setInterpolator(
                    AnimUtils.getLinearOutSlowInInterpolator(sceneRoot.getContext()));
        }
        circularReveal.setDuration(240L);

        final Animator translate = ObjectAnimator.ofFloat(
                view,
                View.TRANSLATION_X,
                View.TRANSLATION_Y,
                fabToDialog ? getPathMotion().getPath(translationX, translationY, 0, 0)
                        : getPathMotion().getPath(0, 0, -translationX, -translationY));
        translate.setDuration(240L);
        translate.setInterpolator(fastOutSlowInInterpolator);

        List<Animator> fadeContents = null;
        if (view instanceof ViewGroup) {
            final ViewGroup vg = ((ViewGroup) view);
            fadeContents = new ArrayList<>(vg.getChildCount());
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                final View child = vg.getChildAt(i);
                final Animator fade =
                        ObjectAnimator.ofFloat(child, View.ALPHA, fabToDialog ? 1f : 0f);
                if (fabToDialog) {
                    child.setAlpha(0f);
                }
                fade.setDuration(160L);
                fade.setInterpolator(fastOutSlowInInterpolator);
                fadeContents.add(fade);
            }
        }

        final Animator colorFade = ObjectAnimator.ofArgb(fabColor, "alpha", fabToDialog ? 0 : 255);
        final Animator iconFade = ObjectAnimator.ofInt(fabIcon, "alpha", fabToDialog ? 0 : 255);
        if (!fabToDialog) {
            colorFade.setStartDelay(120L);
            iconFade.setStartDelay(120L);
        }
        colorFade.setDuration(120L);
        iconFade.setDuration(120L);
        colorFade.setInterpolator(fastOutSlowInInterpolator);
        iconFade.setInterpolator(fastOutSlowInInterpolator);

        final AnimatorSet transition = new AnimatorSet();
        transition.playTogether(circularReveal, translate, colorFade, iconFade);
        transition.playTogether(fadeContents);
        if (fabToDialog) {
            transition.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.getOverlay().clear();
                }
            });
        }
        return new AnimUtils.NoPauseAnimator(transition);
    }

    private void captureValues(TransitionValues transitionValues) {
        final View view = transitionValues.view;
        if (view == null || view.getWidth() <= 0 || view.getHeight() <= 0) return;

        transitionValues.values.put(PROP_BOUNDS, new Rect(view.getLeft(), view.getTop(),
                view.getRight(), view.getBottom()));
    }
}
