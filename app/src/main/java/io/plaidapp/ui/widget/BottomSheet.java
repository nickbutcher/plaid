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
import android.content.res.TypedArray;
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

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.R;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ViewOffsetHelper;

/**
 * A {@link FrameLayout} which can be dragged downward to be dismissed (either directly or via a
 * specified nested scrolling child).  It must contain a single child view and exposes
 * {@link Callbacks} to react to it's movement & dismissal.
 *
 * <p>View dragging has the benefit of reporting it's velocity allowing us to respond to flings etc
 * but does not allow children to scroll.  Nested scrolling allows child views to scroll (duh)
 * but does not report velocity. We combine both to get the best experience we can with the APIs.
 *
 * <p>These two approaches can be in tension so we prefer nested scrolling where possible as it allows
 * switching from scrolling content to moving the sheet in a single gesture.
 */
public class BottomSheet extends FrameLayout {

    // configurable attributes
    private int dismissDistance = Integer.MAX_VALUE;
    private boolean hasScrollingChild = false;
    private int scrollingChildId = -1;

    // child views & helpers
    private View sheet;
    private View scrollingChild;
    private ViewDragHelper sheetDragHelper;
    private ViewOffsetHelper sheetOffsetHelper;

    // state
    private final int FLING_VELOCITY;
    private List<Callbacks> callbacks;
    private int sheetExpandedTop;
    private int sheetBottom;
    private int nestedScrollInitialTop;
    private boolean settling;
    private boolean isNestedScrolling = false;
    private boolean initialHeightChecked = false;

    public BottomSheet(Context context) {
        this(context, null, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomSheet(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        FLING_VELOCITY = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
        final TypedArray a =
                getContext().obtainStyledAttributes(attrs, R.styleable.BottomSheet, 0, 0);

        if (a.hasValue(R.styleable.BottomSheet_scrollingChild)) {
            hasScrollingChild = true;
            scrollingChildId = a.getResourceId(R.styleable.BottomSheet_scrollingChild,
                    scrollingChildId);
        }
        if (a.hasValue(R.styleable.BottomSheet_dragDismissDistance)) {
            dismissDistance = a.getDimensionPixelSize(
                    R.styleable.BottomSheet_dragDismissDistance, dismissDistance);
        }
        a.recycle();
    }

    /**
     * Callbacks for responding to interactions with the bottom sheet.
     */
    public static abstract class Callbacks {
        public void onSheetDismissed() { }
        public void onSheetPositionChanged(int sheetTop) { }
    }

    public void registerCallback(Callbacks callback) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(callback);
    }

    public void unregisterCallback(Callbacks callback) {
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(callback);
        }
    }

    public void dismiss() {
        animateSettle(true);
    }

    public void expand() {
        animateSettle(false);
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
        if (hasScrollingChild) {
            scrollingChild = sheet.findViewById(scrollingChildId);
            if (scrollingChild == null || !scrollingChild.isNestedScrollingEnabled()) {
                throw new RuntimeException("Nested scrolling child specified but not found");
            }
        }
        // force the sheet contents to be gravity bottom. This ain't a top sheet.
        ((LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        super.addView(child, index, params);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (sheet != null && sheet.isLaidOut()) {
            sheetExpandedTop = sheet.getTop();
            sheetBottom = sheet.getBottom();
            sheetOffsetHelper.onViewLayout();

            if (!initialHeightChecked) {
                // bottom sheet content should not initially be taller than the 16:9 keyline
                final int minimumGap = sheet.getMeasuredWidth() / 16 * 9;
                final int gap = getMeasuredHeight() - sheet.getMeasuredHeight();
                if (gap < minimumGap) {
                    sheetOffsetHelper.setTopAndBottomOffset(minimumGap - gap);
                }
                initialHeightChecked = true;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isNestedScrolling) return false; // prefer nested scrolling to dragging

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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        sheetDragHelper = ViewDragHelper.create(this, dragHelperCallbacks);
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
        final int distanceDragged = sheet.getTop() - nestedScrollInitialTop;
        nestedScrollInitialTop = 0;
        if (distanceDragged == 0) return;

        // check if we should perform a dismiss or settle back into place
        final boolean dismiss = distanceDragged >= dismissDistance;
        animateSettle(dismiss);
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
                callback.onSheetPositionChanged(sheet.getTop());
            }
        }
    }

    private boolean isDraggableViewUnder(int x, int y) {
        return getVisibility() == VISIBLE && sheetDragHelper.isViewUnder(this, x, y);
    }

    private void animateSettle(final boolean dismiss) {
        if (settling) return;

        // animate the offset from expanded position
        final int targetOffset = dismiss ? (sheetBottom - sheetExpandedTop) : 0;
        if (sheetOffsetHelper.getTopAndBottomOffset() == targetOffset) return;

        settling = true;
        final ObjectAnimator settleAnim = ObjectAnimator.ofInt(sheetOffsetHelper,
                ViewOffsetHelper.OFFSET_Y,
                sheetOffsetHelper.getTopAndBottomOffset(),
                targetOffset);
        settleAnim.setDuration(200L);
        settleAnim.setInterpolator(dismiss ? AnimUtils.getFastOutLinearInInterpolator(getContext())
                : AnimUtils.getFastOutSlowInInterpolator(getContext()));
        settleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dismiss) {
                    dispatchDismissCallback();
                }
                settling = false;
                dispatchPositionChangedCallback();
            }
        });
        if (callbacks != null && !callbacks.isEmpty()) {
            settleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    dispatchPositionChangedCallback();
                }
            });
        }
        settleAnim.start();
    }

    private ViewDragHelper.Callback dragHelperCallbacks = new ViewDragHelper.Callback() {

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
            if (velocityY >= FLING_VELOCITY) {
                sheetDragHelper.settleCapturedViewAt(sheet.getLeft(), sheetBottom);
            } else {
                // settle back into position
                sheetDragHelper.settleCapturedViewAt(sheet.getLeft(), sheetExpandedTop);
            }
            ViewCompat.postInvalidateOnAnimation(BottomSheet.this);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_IDLE && sheet.getTop() == sheetBottom) {
                dispatchDismissCallback();
            }
        }
    };
}
