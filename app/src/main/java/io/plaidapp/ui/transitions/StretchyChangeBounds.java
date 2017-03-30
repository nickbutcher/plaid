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
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import io.plaidapp.R;
import io.plaidapp.util.AnimUtils;

/**
 * A transition for animating a move/resize of a solid color rectangle. The target view's background
 * is hidden and a fake drawable is placed in the view's overlay for the duration of the transition.
 * The leading and trailing edges animate with different durations and interpolators,
 * creating a stretch effect.
 */
public class StretchyChangeBounds extends Transition {

    private static final String PROPNAME_BOUNDS = "plaid:stretchychangebounds:bounds";
    private static final String[] PROPERTIES = { PROPNAME_BOUNDS };

    private @ColorInt int color = Color.MAGENTA;
    private float trailingSpeed = 0.7f;         // DPs per ms
    private long minTrailingDuration = 200;     // ms
    private long maxTrailingDuration = 400;     // ms
    private long leadingDuration = 200;         // ms

    public StretchyChangeBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StretchyChangeBounds);
        color = a.getColor(R.styleable.StretchyChangeBounds_android_color, color);
        trailingSpeed = a.getFloat(R.styleable.StretchyChangeBounds_trailingSpeed, trailingSpeed);
        minTrailingDuration = a.getInt(R.styleable.StretchyChangeBounds_minTrailingDuration,
                (int) minTrailingDuration);
        maxTrailingDuration = a.getInt(R.styleable.StretchyChangeBounds_maxTrailingDuration,
                (int) maxTrailingDuration);
        leadingDuration = a.getInt(R.styleable.StretchyChangeBounds_leadingDuration,
                (int) leadingDuration);
        a.recycle();
    }

    @Override
    public String[] getTransitionProperties() {
        return PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues) {
        transitionValues.values.put(PROPNAME_BOUNDS, getBoundsInWindow(transitionValues.view));
    }

    /**
     * Because we use this transition on a view whose parent is also transitioning, we capture
     * bounds in window co-ordinates, so that they are not relative to a shifting point.
     */
    @NonNull
    private Rect getBoundsInWindow(View view) {
        int[] loc = new int[2];
        view.getLocationInWindow(loc);
        int y = Math.max(loc[1], 0);
        return new Rect(loc[0], y,
                loc[0] + view.getWidth(),
                y + view.getHeight() + (int) view.getTranslationY());
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues) {
        final View view = endValues.view;
        final ViewGroup parent = ((ViewGroup) view.getParent());
        final Rect startBounds = (Rect) startValues.values.get(PROPNAME_BOUNDS);
        final Rect endBounds = (Rect) endValues.values.get(PROPNAME_BOUNDS);

        // as the captured bounds are in window-space, adjust them to local bounds
        int dx = Math.max(view.getLeft(), 0) - endBounds.left;
        int dy = Math.max(view.getTop(), 0) - endBounds.top;
        startBounds.offset(dx, dy);
        endBounds.offset(dx, dy);

        // hide the view during the transition and let us draw outside of our bounds
        final Drawable background = view.getBackground();
        view.setBackground(null);
        final ViewOutlineProvider outlineProvider = view.getOutlineProvider();
        view.setOutlineProvider(null);
        final boolean clipChildren = parent.getClipChildren();
        parent.setClipChildren(false);

        // use our own drawable in the overlay which we can reposition without thrashing layout
        StretchColorDrawable drawable = new StretchColorDrawable(color);
        drawable.setBounds(startBounds);
        view.getOverlay().add(drawable);

        // work out the direction and size change,
        // use this to determine which edges are leading vs trailing.
        boolean upward = startBounds.centerY() > endBounds.centerY();
        boolean expanding = startBounds.width() < endBounds.width();
        Interpolator fastOutSlowInInterpolator =
                AnimUtils.getFastOutSlowInInterpolator(sceneRoot.getContext());
        Interpolator slowOutFastInInterpolator = AnimationUtils.loadInterpolator(
                sceneRoot.getContext(), R.interpolator.slow_out_fast_in);
        AnimatorSet transition = new AnimatorSet();
        long trailingDuration =
                calculateTrailingDuration(startBounds, endBounds, sceneRoot.getContext());

        Animator leadingEdges, trailingEdges;
        if (expanding) {
            // expanding, left/right move at speed of leading edge
            PropertyValuesHolder left = PropertyValuesHolder.ofInt(StretchColorDrawable.LEFT,
                    startBounds.left, endBounds.left);
            PropertyValuesHolder right = PropertyValuesHolder.ofInt(StretchColorDrawable.RIGHT,
                    startBounds.right, endBounds.right);
            PropertyValuesHolder leadingEdge = PropertyValuesHolder.ofInt(
                    upward ? StretchColorDrawable.TOP : StretchColorDrawable.BOTTOM,
                    upward ? startBounds.top : startBounds.bottom,
                    upward ? endBounds.top : endBounds.bottom);
            leadingEdges =
                    ObjectAnimator.ofPropertyValuesHolder(drawable, left, right, leadingEdge);
            leadingEdges.setDuration(leadingDuration);
            leadingEdges.setInterpolator(fastOutSlowInInterpolator);

            trailingEdges = ObjectAnimator.ofInt(drawable,
                    upward ? StretchColorDrawable.BOTTOM : StretchColorDrawable.TOP,
                    upward ? startBounds.bottom : startBounds.top,
                    upward ? endBounds.bottom : endBounds.top);
            trailingEdges.setDuration(trailingDuration);
            trailingEdges.setInterpolator(slowOutFastInInterpolator);
        } else {
            // contracting, left/right move at speed of trailing edge
            leadingEdges =
                    ObjectAnimator.ofInt(drawable,
                            upward ? StretchColorDrawable.TOP : StretchColorDrawable.BOTTOM,
                            upward ? startBounds.top : startBounds.bottom,
                            upward ? endBounds.top : endBounds.bottom);
            leadingEdges.setDuration(leadingDuration);
            leadingEdges.setInterpolator(fastOutSlowInInterpolator);

            PropertyValuesHolder left = PropertyValuesHolder.ofInt(StretchColorDrawable.LEFT,
                    startBounds.left, endBounds.left);
            PropertyValuesHolder right = PropertyValuesHolder.ofInt(StretchColorDrawable.RIGHT,
                    startBounds.right, endBounds.right);
            PropertyValuesHolder trailingEdge = PropertyValuesHolder.ofInt(
                    upward ? StretchColorDrawable.BOTTOM : StretchColorDrawable.TOP,
                    upward ? startBounds.bottom : startBounds.top,
                    upward ? endBounds.bottom : endBounds.top);

            trailingEdges =
                    ObjectAnimator.ofPropertyValuesHolder(drawable, left, right, trailingEdge);
            trailingEdges.setDuration(trailingDuration);
            trailingEdges.setInterpolator(slowOutFastInInterpolator);
        }

        transition.playTogether(leadingEdges, trailingEdges);
        transition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // clean up
                parent.setClipChildren(clipChildren);
                view.setBackground(background);
                view.setOutlineProvider(outlineProvider);
                view.getOverlay().clear();
            }
        });
        return transition;
    }

    @Override
    public Transition setDuration(long duration) {
        /* don't call super as we want to handle duration ourselves */
        return this;
    }

    /**
     * Calculate the duration for the transition depending upon how far we're moving.
     */
    private long calculateTrailingDuration(
            @NonNull Rect startPosition,
            @NonNull Rect endPosition,
            @NonNull Context context) {
        if (minTrailingDuration == maxTrailingDuration) return minTrailingDuration;

        float pxDistance = (float) Math.hypot(
                startPosition.exactCenterX() - endPosition.exactCenterX(),
                startPosition.exactCenterY() - endPosition.exactCenterY());
        float dpDistance = pxDistance / context.getResources().getDisplayMetrics().density;
        long duration = (long) (dpDistance / trailingSpeed);
        return Math.max(minTrailingDuration, Math.min(maxTrailingDuration, duration));
    }

    /**
     * An extension to {@link ColorDrawable} with convenience methods and properties for easily
     * animating its position and size.
     */
    private static class StretchColorDrawable extends ColorDrawable {

        static final Property<StretchColorDrawable, Integer> LEFT
                = AnimUtils.createIntProperty(new AnimUtils.IntProp<StretchColorDrawable>("left") {
                    @Override
                    public void set(StretchColorDrawable drawable, int left) {
                        drawable.setLeft(left);
                    }

                    @Override
                    public int get(StretchColorDrawable drawable) {
                        return drawable.getLeft();
                    }
                });

        static final Property<StretchColorDrawable, Integer> TOP
                = AnimUtils.createIntProperty(new AnimUtils.IntProp<StretchColorDrawable>("top") {
                    @Override
                    public void set(StretchColorDrawable drawable, int top) {
                        drawable.setTop(top);
                    }

                    @Override
                    public int get(StretchColorDrawable drawable) {
                        return drawable.getTop();
                    }
                });

        static final Property<StretchColorDrawable, Integer> RIGHT
                = AnimUtils.createIntProperty(new AnimUtils.IntProp<StretchColorDrawable>("right") {
                    @Override
                    public void set(StretchColorDrawable drawable, int right) {
                        drawable.setRight(right);
                    }

                    @Override
                    public int get(StretchColorDrawable drawable) {
                        return drawable.getRight();
                    }
                });

        static final Property<StretchColorDrawable, Integer> BOTTOM
               = AnimUtils.createIntProperty(new AnimUtils.IntProp<StretchColorDrawable>("bottom") {
                    @Override
                    public void set(StretchColorDrawable drawable, int bottom) {
                        drawable.setBottom(bottom);
                    }

                    @Override
                    public int get(StretchColorDrawable drawable) {
                        return drawable.getBottom();
                    }
                });

        private int left, top, right, bottom;

        StretchColorDrawable(@ColorInt int color) {
            super(color);
        }

        int getLeft() {
            return left;
        }

        void setLeft(int left) {
            this.left = left;
            updateBounds();
        }

        int getTop() {
            return top;
        }

        void setTop(int top) {
            this.top = top;
            updateBounds();
        }

        int getRight() {
            return right;
        }

        void setRight(int right) {
            this.right = right;
            updateBounds();
        }

        int getBottom() {
            return bottom;
        }

        void setBottom(int bottom) {
            this.bottom = bottom;
            updateBounds();
        }

        private void updateBounds() {
            setBounds(left, top, right, bottom);
            invalidateSelf();
        }
    }
}
