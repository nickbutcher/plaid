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

package io.plaidapp.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.MathUtils;
import io.plaidapp.util.ViewOffsetHelper;

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
    private static final long DEFAULT_SETTLE_DURATION = 300L;   // ms
    private static final long MIN_SETTLE_DURATION = 50L;        // ms

    // config
    private final int MIN_FLING_VELOCITY;
    private final int MAX_FLING_VELOCITY;

    // child views & helpers
    private View sheet;
    private ViewDragHelper sheetDragHelper;
    private ViewOffsetHelper sheetOffsetHelper;

    // state
    private List<Callbacks> callbacks;
    private int sheetExpandedTop;
    private int sheetBottom;
    private int dismissOffset;
    private int nestedScrollInitialTop;
    private boolean settling = false;
    private boolean isNestedScrolling = false;
    private boolean initialHeightChecked = false;
    private boolean hasInteractedWithSheet = false;

    public BottomSheet(Context context) {
        this(context, null, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        MIN_FLING_VELOCITY = viewConfiguration.getScaledMinimumFlingVelocity();
        MAX_FLING_VELOCITY = viewConfiguration.getScaledMaximumFlingVelocity();
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
        animateSettle(dismissOffset);
    }

    public void expand() {
        animateSettle(0);
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

        final int action = MotionEventCompat.getActionMasked(ev);
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
        if (sheetDragHelper.getCapturedView() != null) {
            return true;
        }
        return super.onTouchEvent(ev);
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
        if (velocityY <= -MIN_FLING_VELOCITY           /* flinging downward */
                && !target.canScrollVertically(-1)) {  /* nested scrolling child can't scroll up */
            animateSettle(dismissOffset, computeSettleDuration(velocityY, true));
            return true;
        } else if (velocityY > 0 && !isExpanded()) {
            animateSettle(0, computeSettleDuration(velocityY, false));
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

    private void animateSettle(int targetOffset) {
        animateSettle(targetOffset, DEFAULT_SETTLE_DURATION);
    }

    private void animateSettle(int targetOffset, long duration) {
        animateSettle(sheetOffsetHelper.getTopAndBottomOffset(), targetOffset, duration);
    }

    private void animateSettle(int initialOffset, final int targetOffset, long duration) {
        if (settling) return;
        if (sheetOffsetHelper.getTopAndBottomOffset() == targetOffset) {
          if (targetOffset >= dismissOffset) {
              dispatchDismissCallback();
          }
          return;
        }

        settling = true;
        final ObjectAnimator settleAnim = ObjectAnimator.ofInt(sheetOffsetHelper,
                ViewOffsetHelper.OFFSET_Y,
                initialOffset,
                targetOffset);
        settleAnim.setDuration(duration);
        settleAnim.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(getContext()));
        settleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dispatchPositionChangedCallback();
                if (targetOffset == dismissOffset) {
                    dispatchDismissCallback();
                }
                settling = false;
            }
        });
        if (callbacks != null && !callbacks.isEmpty()) {
            settleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedFraction() > 0f) {
                        dispatchPositionChangedCallback();
                    }
                }
            });
        }
        settleAnim.start();
    }

    /**
     * Calculate the duration of the settle animation based on the gesture velocity
     * and how far it has to go.
     */
    private long computeSettleDuration(final float velocity, final boolean dismissing) {
        final float settleDistance = dismissing ?
                sheetBottom - sheet.getTop() : sheet.getTop() - sheetExpandedTop;
        final float clampedVelocity =
                MathUtils.constrain(MIN_FLING_VELOCITY, MAX_FLING_VELOCITY, Math.abs(velocity));
        final float distanceFraction = settleDistance / (sheetBottom - sheetExpandedTop);
        final float velocityFraction = clampedVelocity / MAX_FLING_VELOCITY;
        final long duration = MIN_SETTLE_DURATION +
                (long) (distanceFraction * (1f - velocityFraction) * DEFAULT_SETTLE_DURATION);
        return duration;
    }

    private final ViewDragHelper.Callback dragHelperCallbacks = new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == sheet;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.min(Math.max(top, sheetExpandedTop), sheetBottom);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return sheet.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return sheetBottom - sheetExpandedTop;
        }

        @Override
        public void onViewPositionChanged(View child, int left, int top, int dx, int dy) {
            // notify the offset helper that the sheets offsets have been changed externally
            sheetOffsetHelper.resyncOffsets();
            dispatchPositionChangedCallback();
        }

        @Override
        public void onViewReleased(View releasedChild, float velocityX, float velocityY) {
            // dismiss on downward fling, otherwise settle back to expanded position
            final boolean dismiss = velocityY >= MIN_FLING_VELOCITY;
            animateSettle(dismiss ? dismissOffset : 0, computeSettleDuration(velocityY, dismiss));
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

    private void applySheetInitialHeightOffset(boolean animateChange, int previousOffset) {
        final int minimumGap = sheet.getMeasuredWidth() / 16 * 9;
        if (sheet.getTop() < minimumGap) {
            final int offset = minimumGap - sheet.getTop();
            if (animateChange) {
                animateSettle(previousOffset, offset, DEFAULT_SETTLE_DURATION);
            } else {
                sheetOffsetHelper.setTopAndBottomOffset(offset);
            }
        }
    }

    private void dispatchDismissCallback() {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (Callbacks callback : callbacks) {
                callback.onSheetDismissed();
            }
        }
    }

    private void dispatchPositionChangedCallback() {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (Callbacks callback : callbacks) {
                callback.onSheetPositionChanged(sheet.getTop(), hasInteractedWithSheet);
            }
        }
    }
}
