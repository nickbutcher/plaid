/*
 * Copyright 2018 Google LLC
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership. The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.plaidapp.core.ui.widget;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.plaidapp.core.util.ViewOffsetHelper;

/**
 * A {@link FrameLayout} whose content can be dragged downward to be dismissed (either directly or
 * via a nested scrolling child). It must contain a single child view and exposes {@link Callbacks}
 * to respond to it's movement & dismissal.
 *
 * Only implements the modal bottom sheet behavior from the material spec, not the persistent
 * behavior (yet).
 */
public class BottomSheet extends FrameLayout {

    // constants
    private static final float SETTLE_STIFFNESS = 800f;
    private static final int MIN_FLING_DISMISS_VELOCITY = 500; // dp/s
    private final int SCALED_MIN_FLING_DISMISS_VELOCITY; // px/s

    // child views & helpers
    View sheet;
    ViewOffsetHelper sheetOffsetHelper;
    private ViewDragHelper sheetDragHelper;

    // state
    int sheetExpandedTop;
    int sheetBottom;
    int dismissOffset;
    boolean settling = false;
    boolean initialHeightChecked = false;
    boolean hasInteractedWithSheet = false;
    private int nestedScrollInitialTop;
    private boolean isNestedScrolling = false;
    private List<Callbacks> callbacks;

    public BottomSheet(Context context) {
        this(context, null, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        SCALED_MIN_FLING_DISMISS_VELOCITY =
            (int) (context.getResources().getDisplayMetrics().density * MIN_FLING_DISMISS_VELOCITY);
    }

    /**
     * Callbacks for responding to interactions with the bottom sheet.
     */
    public static abstract class Callbacks {
        public void onSheetDismissed() { }
        public void onSheetPositionChanged(int sheetTop, boolean userInteracted) { }
    }

    public void registerCallback(Callbacks callback) {
        if (callbacks == null) {
            callbacks = new CopyOnWriteArrayList<>();
        }
        callbacks.add(callback);
    }

    public void unregisterCallback(Callbacks callback) {
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(callback);
        }
    }

    public void dismiss() {
        animateSettle(dismissOffset, 0);
    }

    public void expand() {
        animateSettle(0, 0);
    }

    public boolean isExpanded() {
        return sheet.getTop() == sheetExpandedTop;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (sheet != null) {
            throw new UnsupportedOperationException("BottomSheet must only have 1 child view");
        }
        sheet = child;
        sheetOffsetHelper = new ViewOffsetHelper(sheet);
        sheet.addOnLayoutChangeListener(sheetLayout);
        // force the sheet contents to be gravity bottom. This ain't a top sheet.
        ((LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        super.addView(child, index, params);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        hasInteractedWithSheet = true;
        if (isNestedScrolling) return false;    /* prefer nested scrolling to dragging */

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            sheetDragHelper.cancel();
            return false;
        }
        return isDraggableViewUnder((int) ev.getX(), (int) ev.getY())
                && (sheetDragHelper.shouldInterceptTouchEvent(ev));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        sheetDragHelper.processTouchEvent(ev);
        return sheetDragHelper.getCapturedView() != null || super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (sheetDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if ((nestedScrollAxes & View.SCROLL_AXIS_VERTICAL) != 0) {
            isNestedScrolling = true;
            nestedScrollInitialTop = sheet.getTop();
            return true;
        }
        return false;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        // if scrolling downward, use any unconsumed (i.e. not used by the scrolling child)
        // to drag the sheet downward
        if (dyUnconsumed < 0) {
            sheetOffsetHelper.offsetTopAndBottom(-dyUnconsumed);
            dispatchPositionChangedCallback();
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // if scrolling upward & the sheet has been dragged downward
        // then drag back into place before allowing scrolls
        if (dy > 0) {
            final int upwardDragRange = sheet.getTop() - sheetExpandedTop;
            if (upwardDragRange > 0) {
                final int consume = Math.min(upwardDragRange, dy);
                sheetOffsetHelper.offsetTopAndBottom(-consume);
                dispatchPositionChangedCallback();
                consumed[1] = consume;
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        isNestedScrolling = false;
        if (!settling                                               /* fling might have occurred */
                && sheet.getTop() != nestedScrollInitialTop) {      /* don't expand after a tap */
            expand();
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (velocityY <= -SCALED_MIN_FLING_DISMISS_VELOCITY   /* flinging downward */
                && !target.canScrollVertically(-1)) {   /* nested scrolling child can't scroll up */
            animateSettle(dismissOffset, velocityY);
            return true;
        } else if (velocityY > 0 && !isExpanded()) {
            animateSettle(0, velocityY);
        }
        return false;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        sheetDragHelper = ViewDragHelper.create(this, dragHelperCallbacks);
    }

    private boolean isDraggableViewUnder(int x, int y) {
        return getVisibility() == VISIBLE && sheetDragHelper.isViewUnder(this, x, y);
    }

    void animateSettle(int targetOffset, float initialVelocity) {
        if (settling) return;
        if (sheetOffsetHelper.getTopAndBottomOffset() == targetOffset) {
          if (targetOffset >= dismissOffset) {
              dispatchDismissCallback();
          }
          return;
        }

        settling = true;
        final boolean dismissing = targetOffset == dismissOffset;
        // if we're dismissing, we don't want the view to decelerate as it reaches the bottom
        // so set a target position that actually overshoots a little
        final float finalPosition = dismissing ? dismissOffset * 1.1f : targetOffset;

        SpringAnimation anim = new SpringAnimation(
                sheetOffsetHelper, ViewOffsetHelper.OFFSET_Y, finalPosition)
                .setStartValue(sheetOffsetHelper.getTopAndBottomOffset())
                .setStartVelocity(initialVelocity)
                .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS);
        anim.getSpring()
                .setStiffness(SETTLE_STIFFNESS)
                .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY);
        anim.addEndListener((animation, canceled, value, velocity) -> {
            dispatchPositionChangedCallback();
            if (dismissing) {
                dispatchDismissCallback();
            }
            settling = false;
        });
        if (callbacks != null && !callbacks.isEmpty()) {
            anim.addUpdateListener((animation, value, velocity) -> dispatchPositionChangedCallback());
        }
        anim.start();
    }

    private final ViewDragHelper.Callback dragHelperCallbacks = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
            return child == sheet;
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            return Math.min(Math.max(top, sheetExpandedTop), sheetBottom);
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            return sheet.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return sheetBottom - sheetExpandedTop;
        }

        @Override
        public void onViewPositionChanged(@NonNull View child, int left, int top, int dx, int dy) {
            // notify the offset helper that the sheets offsets have been changed externally
            sheetOffsetHelper.resyncOffsets();
            dispatchPositionChangedCallback();
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float velocityX, float velocityY) {
            // dismiss on downward fling, otherwise settle back to expanded position
            final boolean dismiss = velocityY >= SCALED_MIN_FLING_DISMISS_VELOCITY;
            animateSettle(dismiss ? dismissOffset : 0, velocityY);
        }

    };

    private final OnLayoutChangeListener sheetLayout = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            sheetExpandedTop = top;
            sheetBottom = bottom;
            dismissOffset = bottom - top;
            sheetOffsetHelper.onViewLayout();

            // modal bottom sheet content should not initially be taller than the 16:9 keyline
            if (!initialHeightChecked) {
                applySheetInitialHeightOffset(false, -1);
                initialHeightChecked = true;
            } else if (!hasInteractedWithSheet
                    && (oldBottom - oldTop) != (bottom - top)) { /* sheet height changed */
                /* if the sheet content's height changes before the user has interacted with it
                   then consider this still in the 'initial' state and apply the height constraint,
                   but in this case, animate to it */
                applySheetInitialHeightOffset(true, oldTop - sheetExpandedTop);
            }
        }
    };

    void applySheetInitialHeightOffset(boolean animateChange, int previousOffset) {
        final int minimumGap = sheet.getMeasuredWidth() / 16 * 9;
        if (sheet.getTop() < minimumGap) {
            final int offset = minimumGap - sheet.getTop();
            if (animateChange) {
                sheetOffsetHelper.setTopAndBottomOffset(previousOffset);
                animateSettle(offset, 0);
            } else {
                sheetOffsetHelper.setTopAndBottomOffset(offset);
            }
        }
    }

    void dispatchDismissCallback() {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (Callbacks callback : callbacks) {
                callback.onSheetDismissed();
            }
        }
    }

    void dispatchPositionChangedCallback() {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (Callbacks callback : callbacks) {
                callback.onSheetPositionChanged(sheet.getTop(), hasInteractedWithSheet);
            }
        }
    }
}
