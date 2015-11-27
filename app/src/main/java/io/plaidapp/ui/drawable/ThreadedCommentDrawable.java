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

package io.plaidapp.ui.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;

/**
 * A drawable showing the depth of a threaded conversation
 */
public class ThreadedCommentDrawable extends Drawable {

    private static final @ColorInt int THREAD_COLOR = 0xffeceef1;

    private final int threadWidth;
    private final int gap;
    private final int halfThreadWidth;
    private final Paint paint;
    private int threads;

    /**
     *
     * @param threadWidth in pixels
     * @param gap in pixels
     */
    public ThreadedCommentDrawable(int threadWidth, int gap) {
        this.threadWidth = threadWidth;
        this.gap = gap;
        halfThreadWidth = threadWidth / 2;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(threadWidth);
        paint.setColor(THREAD_COLOR);
    }

    public ThreadedCommentDrawable(int threadWidth, int gap, int depth) {
        this(threadWidth, gap);
        setDepth(depth);
    }

    public void setDepth(int depth) {
        this.threads = depth + 1;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        for (int thread = 0; thread < threads; thread++) {
            int left = halfThreadWidth + (thread * (threadWidth + gap));
            canvas.drawLine(left, 0, left, getBounds().bottom, paint);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return (threads * threadWidth) + ((threads - 1) * gap);
    }

    @Override
    public void setAlpha(int i) {
        paint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThreadedCommentDrawable that = (ThreadedCommentDrawable) o;
        return threads == that.threads;
    }

    @Override
    public int hashCode() {
        return threads;
    }
}
