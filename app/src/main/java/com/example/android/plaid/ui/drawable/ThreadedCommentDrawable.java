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

package com.example.android.plaid.ui.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

/**
 * Created by nickbutcher on 7/22/14.
 */
public class ThreadedCommentDrawable extends Drawable {

    //    private static final int[] threadColours = { 0x200288d1,     // 75% Light Blue 700
//                                                 0x20039be5,     // 75% Light Blue 600
//                                                 0x2003a9f4,     // 75% Light Blue 500
//                                                 0x2029b6f6,     // 75% Light Blue 400
//                                                 0x204fc3f7 };   // 75% Light Blue 300
    private static final int[] threadColours = {0xffECEEF1,
            0xffECEEF1,
            0xffECEEF1,
            0xffECEEF1,
            0xffECEEF1};
    private final int threadWidth;
    private final int gap;
    private final int halfThreadWidth;
    private final Paint paint;
    private int threads;

    public ThreadedCommentDrawable(int threadWidth, int gap) {
        this.threadWidth = threadWidth;
        this.gap = gap;
        halfThreadWidth = threadWidth / 2;
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(threadWidth);
    }

    public void setDepth(int depth) {
        this.threads = depth + 1;
    }

    @Override
    public void draw(Canvas canvas) {
        for (int thread = 0; thread < threads; thread++) {
            paint.setColor(threadColours[thread % threadColours.length]);
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
