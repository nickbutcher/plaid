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

package io.plaidapp.ui.span;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.text.Layout;
import android.text.style.LeadingMarginSpan;

/**
 * Created by nickbutcher on 9/2/14.
 */
public class DesignerNewsQuoteSpan implements LeadingMarginSpan {

    private static final int LINE_WIDTH = 2;            // dp
    private static final int GAP_WIDTH = 16;           // dp
    private static final int LINE_COLOR = 0xffECEEF1;   // match website

    private final int lineColor;
    private final int lineWidth;
    private final int gapWidth;

    public DesignerNewsQuoteSpan(Context context) {
        super();

        final int scaledDensity = (int) context.getResources().getDisplayMetrics().scaledDensity;
        lineWidth = scaledDensity * LINE_WIDTH;
        gapWidth = scaledDensity * GAP_WIDTH;
        lineColor = LINE_COLOR;
    }

    public DesignerNewsQuoteSpan(int quoteLineWidth, int guoteLineIndent, @ColorInt int
            quoteLineColor) {
        super();
        lineWidth = quoteLineWidth;
        gapWidth = guoteLineIndent;
        lineColor = quoteLineColor;
    }

    public int getLeadingMargin(boolean first) {
        return lineWidth + gapWidth;
    }

    public void drawLeadingMargin(Canvas c,
                                  Paint p,
                                  int x,
                                  int dir,
                                  int top,
                                  int baseline,
                                  int bottom,
                                  CharSequence text,
                                  int start,
                                  int end,
                                  boolean first,
                                  Layout layout) {
        Paint.Style prevStyle = p.getStyle();
        int prevColor = p.getColor();
        p.setStyle(Paint.Style.FILL);
        p.setColor(lineColor);
        c.drawRect(x, top, x + dir * lineWidth, bottom, p);
        p.setStyle(prevStyle);
        p.setColor(prevColor);
    }
}
