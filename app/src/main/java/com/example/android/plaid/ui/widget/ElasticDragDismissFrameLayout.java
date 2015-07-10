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

package com.example.android.plaid.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.example.android.plaid.R;

/**
 * Created by nickbutcher on 12/11/14.
 */
public class ElasticDragDismissFrameLayout extends FrameLayout {

    // configurable attribs
    private float dragDismissDistance = 0f;
    private float dragDismissFraction = -1f;
    private boolean clampDragAtDismissDistance = true;
    private float dragElacticity = 1f;
    private float dragDismissScale = 1f;
    private boolean shouldScale = false;

    private float totalDrag;
    private boolean draggingDown = false;
    private boolean draggingUp = false;

    private DismissibleViewCallback callback;

    public ElasticDragDismissFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ElasticDragDismissFrameLayout, 0, 0);

        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissDistance)) {
            dragDismissDistance = a.getDimensionPixelSize(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissDistance, 0);
        } else if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissFraction)) {
            dragDismissFraction = a.getFloat(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissFraction, dragDismissFraction);
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_clampDragAtDismissDistance)) {
            clampDragAtDismissDistance = a.getBoolean(R.styleable
                            .ElasticDragDismissFrameLayout_clampDragAtDismissDistance,
                    clampDragAtDismissDistance);
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragElasticity)) {
            dragElacticity = a.getFloat(R.styleable.ElasticDragDismissFrameLayout_dragElasticity,
                    dragElacticity);
        }
        if (a.hasValue(R.styleable.ElasticDragDismissFrameLayout_dragDismissScale)) {
            dragDismissScale = a.getFloat(R.styleable
                    .ElasticDragDismissFrameLayout_dragDismissScale, dragDismissScale);
            shouldScale = dragDismissScale != 1f;
        }
        a.recycle();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
            dyUnconsumed) {
        dragScale(dyUnconsumed);
    }

    @Override
    public void onStopNestedScroll(View child) {
        if (Math.abs(totalDrag) >= dragDismissDistance) {
            dispatchDismissCallback();
        } else { // settle back to natural position
            animate()
                    .translationY(0f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200L)
                    .setInterpolator(AnimationUtils.loadInterpolator(getContext(), android.R
                            .interpolator.fast_out_slow_in))
                    .setListener(null)
                    .start();
        }
        totalDrag = 0;
        draggingDown = draggingUp = false;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // if we're in a drag gesture and the user reverses up the we should take those events
        if (draggingDown && dy > 0 || draggingUp && dy < 0) {
            dragScale(dy);
            consumed[1] = dy;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (dragDismissFraction > 0f) {
            dragDismissDistance = h * dragDismissFraction;
        }
    }

    public DismissibleViewCallback getCallback() {
        return callback;
    }

    public void setCallback(DismissibleViewCallback callback) {
        this.callback = callback;
    }

    private void dragScale(int scroll) {
        if (scroll != 0) {

            // track the direction & set the pivot point for scaling
            // don't double track i.e. if start dragging down and then reverse, keep tracking as
            // dragging down until they reach the 'natural' position
            if (scroll < 0 && !draggingUp) {
                draggingDown = true;
                if (shouldScale) setPivotY(getHeight());
            } else if (scroll > 0 && !draggingDown) {
                draggingUp = true;
                if (shouldScale) setPivotY(0);
            }
            totalDrag += scroll;

            // how far have we dragged relative to the distance to perform a dismiss (0–1 where 1
            // = dismiss distance)
            float dragFraction = 1 - ((dragDismissDistance - Math.min(Math.abs(totalDrag),
                    dragDismissDistance)) / dragDismissDistance);
            float dragTo = totalDrag;
            if (clampDragAtDismissDistance) {
                dragTo = (dragFraction * dragDismissDistance);
                if (draggingUp) { // as we use the absolute magnitude when calculating the drag
                    // fraction, need to re-apply the drag direction
                    dragTo *= -1;
                }
            } else {
                dragTo *= -1; // convert from scroll direction to translate direction
            }
            dragTo *= dragElacticity;

            // clamp the values so that we don't scroll too far
//            if (draggingDown) {
//                dragTo = Math.max(dragTo, 0f);
//            } else if (draggingUp) {
//                dragTo = Math.min(dragTo, 0f);
//            }
            setTranslationY(dragTo);

            if (shouldScale) {
                float scale = 1 - ((1 - dragDismissScale) * dragFraction);
                setScaleX(scale);
                setScaleY(scale);
            }

            // if we've reversed direction and gone past the settle point then clear the flags to
            // allow the list to get the scroll events & reset any transforms
            if ((draggingDown && totalDrag >= 0)
                    || (draggingUp && totalDrag <= 0)) {
                totalDrag = 0;
                draggingDown = draggingUp = false;
                setTranslationY(0f);
                setScaleX(1f);
                setScaleY(1f);
            }
        }
    }

    private void dispatchDismissCallback() {
        if (callback != null) {
            callback.onViewDismissed();
        }
    }

}
