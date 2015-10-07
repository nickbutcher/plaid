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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by nickbutcher on 7/31/14.
 */
public class DragDownDismissFrameLayout extends FrameLayout {

    protected Callbacks callbacks;
    //    private final ElasticViewDragHelper viewDragHelper;
//    private final int FLING_VELOCITY;
    private View dragView;
    private int dragRange;
    private int dragViewTop;
    private float dragOffset;
    private int top;
    private int bottom;
    private boolean isDismissing;

    public DragDownDismissFrameLayout(Context context) {
        this(context, null, 0);
    }

    public DragDownDismissFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragDownDismissFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //viewDragHelper = ElasticViewDragHelper.create(this, dragHelperCallbacks);
        //FLING_VELOCITY = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
    }

    public void dispatchDismissCallback() {
        if (callbacks != null) {
            callbacks.onViewDismissed(top, bottom);
        }
    }

    public void setCallbacks(Callbacks callbacks) {
        this.callbacks = callbacks;
    }
/*
    public void dispatchDragCallback() {
        if (callbacks != null) {
            callbacks.onDrag(dragViewTop);
        }
    }

    public void setDragDismissView(View draggableView) {
        this.dragView = draggableView;
    }

    public View getDragDismissView() {
        return dragView;
    }*/

    public interface Callbacks {
        boolean shouldCapture();

        void onViewDismissed(int viewTop, int viewBottom);

        void onDrag(int top);
    }
/*
    public void onDrag(int top, float offset) {
        if (offset > 0.5f) {
            dragView.setAlpha(1.5f - offset);
            dragView.invalidate();
        } else if (dragView.getAlpha() != 1.0f) {
            dragView.setAlpha(1.0f);
        }
    }

    public boolean dragReleaseShouldDismiss(boolean flingDown, int dragged) {
         return flingDown;
    }

    public void doDismiss() {
        viewDragHelper.settleCapturedViewAt(0, bottom);
    }

    private ElasticViewDragHelper.Callback dragHelperCallbacks = new ElasticViewDragHelper
    .Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            boolean capture = callbacks.shouldCapture()
                    && child.getVisibility() == View.VISIBLE;
            return capture;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.min(Math.max(top, DragDownDismissFrameLayout.this.top), bottom);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return dragRange;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            dragViewTop = top;
            dragOffset = (float) (top - DragDownDismissFrameLayout.this.top) / (float) dragRange;
            onDrag(top, dragOffset);
            dispatchDragCallback();
        }

        @Override
        public void onViewReleased(View releasedChild, float velocityX, float velocityY) {
            if (dragReleaseShouldDismiss(velocityY > FLING_VELOCITY, viewDragHelper
            .getLastDragSize())) {
                isDismissing = true;
                doDismiss();
            } else {
                // settle back into position
                viewDragHelper.settleCapturedViewAt(0, top);
            }
            ViewCompat.postInvalidateOnAnimation(DragDownDismissFrameLayout.this);
        }

        @Override
        public void onViewDragStateChanged(int state) {
            if (isDismissing
                    && state == ElasticViewDragHelper.STATE_IDLE) {
                isDismissing = false;
                dispatchDismissCallback();
            }
        }
    };

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            viewDragHelper.cancel();
            return false;
        }
        return isDraggableViewUnder((int) ev.getX(), (int) ev.getY()) && (viewDragHelper
        .shouldInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev));
    }

    private boolean isDraggableViewUnder(int x, int y) {
        return dragView.getVisibility() == VISIBLE
                && viewDragHelper.isViewUnder(dragView, x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        viewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public void computeScroll() {
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.top = dragView.getTop();
        this.bottom = getHeight();
        dragRange = this.bottom - this.top;
    }

    public void setMaxDrag(int maxDrag) {
        viewDragHelper.setMaxDrag(maxDrag);
        viewDragHelper.setElastic(true);
    }

    public int getMaxDrag() {
        return viewDragHelper.getMaxDrag();
    }*/
}
