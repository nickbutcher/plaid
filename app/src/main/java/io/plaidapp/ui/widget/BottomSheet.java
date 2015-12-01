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
 * View dragging has the benefit of reporting it's velocity allowing us to respond to flings etc
 * but does not allow children to scroll.  Nested scrolling allows child views to scroll (duh)
 * but does not report velocity. We combine both to get the best experience we can with the APIs.
 */
public class BottomSheet extends FrameLayout {

    // configurable attributes
    private int dragDismissDistance = Integer.MAX_VALUE;
    private boolean hasScrollingChild = false;
    private int scrollingChildId = -1;

    // child views & helpers
    private View dragView;
    private View scrollingChild;
    private ViewDragHelper viewDragHelper;
    private ViewOffsetHelper dragViewOffsetHelper;

    // state
    private final int FLING_VELOCITY;
    private List<Callbacks> callbacks;
    private boolean isDismissing;
    private int dragViewLeft;
    private int dragViewExpandedTop;
    private int dragViewLastSettledTop;
    private int dragViewBottom;
    private boolean settling;
    private boolean lastNestedScrollWasDownward;
    private boolean scrollingChildTouched;
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
            dragDismissDistance = a.getDimensionPixelSize(
                    R.styleable.BottomSheet_dragDismissDistance, dragDismissDistance);
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

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (dragView != null) {
            throw new UnsupportedOperationException("BottomSheet must only have 1 child view");
        }
        dragView = child;
        dragViewOffsetHelper = new ViewOffsetHelper(dragView);
        if (hasScrollingChild) {
            scrollingChild = dragView.findViewById(scrollingChildId);
            if (scrollingChild == null) {
                throw new RuntimeException("Scrolling child specified but not found");
            }
        }
        // force the sheet contents to be gravity bottom. This ain't a top sheet.
        ((LayoutParams) params).gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        super.addView(child, index, params);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        viewDragHelper = ViewDragHelper.create(this, dragHelperCallbacks);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (dragView != null && ViewCompat.isLaidOut(dragView)) {
            dragViewLeft = dragView.getLeft();
            dragViewExpandedTop = dragView.getTop();
            dragViewBottom = dragView.getBottom();
            dragViewOffsetHelper.onViewLayout();

            if (!initialHeightChecked) {
                // bottom sheet content should not initially be taller than the 16:9 keyline
                final int minimumGap = dragView.getMeasuredWidth() / 16 * 9;
                final int gap = getMeasuredHeight() - dragView.getMeasuredHeight();
                if (gap < minimumGap) {
                    dragViewOffsetHelper.setTopAndBottomOffset(minimumGap - gap);
                }
                dragViewLastSettledTop = dragView.getTop();
                initialHeightChecked = true;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }
        checkScrollingChildHit(ev);
        return isDraggableViewUnder((int) ev.getX(), (int) ev.getY())
            && (viewDragHelper.shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev));
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        checkScrollingChildHit(ev);
        viewDragHelper.processTouchEvent(ev);
        if (viewDragHelper.getCapturedView() == null) {
            return super.onTouchEvent(ev);
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        // if scrolling downward, use any unconsumed (i.e. not used by the scrolling child)
        // to drag the sheet downward
        lastNestedScrollWasDownward = dyUnconsumed < 0;
        if (lastNestedScrollWasDownward) {
            dragView.offsetTopAndBottom(-dyUnconsumed);
            dispatchPositionChangedCallback();
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // if scrolling upward & the sheet has been dragged downward
        // then drag back into place before allowing scrolls
        if (dy > 0) {
            final int upwardDragRange = dragView.getTop() - dragViewExpandedTop;
            if (upwardDragRange > 0) {
                final int consume = Math.min(upwardDragRange, dy);
                dragView.offsetTopAndBottom(-consume);
                dispatchPositionChangedCallback();
                consumed[1] = consume;
                lastNestedScrollWasDownward = false;
            }
        }
    }

    @Override
    public void onStopNestedScroll(View child) {
        final int dragDisplacement = dragView.getTop() - dragViewLastSettledTop;
        if (dragDisplacement == 0) return;

        // check if we should perform a dismiss or settle back into place
        final boolean dismiss =
                lastNestedScrollWasDownward && dragDisplacement >= dragDismissDistance;
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
                callback.onSheetPositionChanged(dragView.getTop());
            }
        }
    }

    private boolean isDraggableViewUnder(int x, int y) {
        return getVisibility() == VISIBLE && viewDragHelper.isViewUnder(this, x, y);
    }

    private void checkScrollingChildHit(MotionEvent ev) {
        // we need to know if the scrolling child was touched in dragHelperCallbacks#tryCaptureView
        // but that does not have access to the motion event, so check earlier & store
        if (hasScrollingChild && MotionEventCompat.getActionMasked(ev) == MotionEvent.ACTION_DOWN) {
            scrollingChildTouched =
                    viewDragHelper.isViewUnder(scrollingChild,
                            (int) ev.getX() - dragView.getLeft(),
                            (int) ev.getY() - dragView.getTop());
        }
    }

    private void animateSettle(final boolean dismiss) {
        if (settling) return;
        final int settleAt = dismiss ? dragViewBottom : dragViewExpandedTop;
        if (dragView.getTop() == settleAt) return;

        // animate either back into place or to bottom
        settling = true;
        final ObjectAnimator settleAnim = ObjectAnimator.ofInt(dragViewOffsetHelper,
                ViewOffsetHelper.OFFSET_Y,
                dragView.getTop(),
                settleAt);
        settleAnim.setDuration(200L);
        settleAnim.setInterpolator(AnimUtils.getFastOutSlowInInterpolator());
        settleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (dismiss) {
                    dispatchDismissCallback();
                } else {
                    dragViewLastSettledTop = dragViewExpandedTop;
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
            if (scrollingChildTouched) {
                // if we have a scrolling child and it can scroll then don't drag, it'll be handled
                // by nested scrolling
                final boolean childCanScroll = scrollingChild.canScrollVertically(1)
                        || scrollingChild.canScrollVertically(-1);
                return !childCanScroll;
            }
            return child == dragView;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.min(Math.max(top, dragViewExpandedTop), dragViewBottom);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return dragViewLeft;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return dragViewBottom - dragViewExpandedTop;
        }

        @Override
        public void onViewPositionChanged(View child, int left, int top, int dx, int dy) {
            dispatchPositionChangedCallback();
        }

        @Override
        public void onViewReleased(View releasedChild, float velocityX, float velocityY) {
            if (velocityY >= FLING_VELOCITY) {
                isDismissing = true;
                viewDragHelper.settleCapturedViewAt(dragViewLeft, dragViewBottom);
            } else {
                // settle back into position
                viewDragHelper.settleCapturedViewAt(dragViewLeft, dragViewExpandedTop);
                dragViewLastSettledTop = dragViewExpandedTop;
            }
            ViewCompat.postInvalidateOnAnimation(BottomSheet.this);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (isDismissing && state == ViewDragHelper.STATE_IDLE) {
                isDismissing = false;
                dispatchDismissCallback();
            }
        }
    };
}
