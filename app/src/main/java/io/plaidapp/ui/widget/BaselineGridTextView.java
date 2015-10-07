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
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

import io.plaidapp.R;

public class BaselineGridTextView extends FontTextView {

    private float lineHeightMultiplierHint = 1f;
    private float lineHeightHint = 0f;
    private int topPaddingHint = 0;

    public BaselineGridTextView(Context context) {
        this(context, null);
    }

    public BaselineGridTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public BaselineGridTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BaselineGridTextView(Context context, AttributeSet attrs,
                                int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.BaselineGridTextView, defStyleAttr, defStyleRes);

        lineHeightMultiplierHint =
                a.getFloat(R.styleable.BaselineGridTextView_lineHeightMultiplierHint, 1f);
        lineHeightHint =
                a.getDimensionPixelSize(R.styleable.BaselineGridTextView_lineHeightHint, 0);
        topPaddingHint =
                a.getDimensionPixelSize(R.styleable.BaselineGridTextView_topPaddingHint, 0);

        a.recycle();

        setIncludeFontPadding(false);
        setElegantTextHeight(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        recomputeLineHeight();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void recomputeLineHeight() {
        float fourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());

        // Ensure that the first line's baselines sits on 4dp grid by setting the top padding
        Paint.FontMetricsInt fm = getPaint().getFontMetricsInt();
        int gridAlignedTopPadding = (int) (fourDip * (float)
                Math.ceil((topPaddingHint + Math.abs(fm.ascent)) / fourDip)
                - Math.ceil(Math.abs(fm.ascent)));
        setPadding(getPaddingLeft(), gridAlignedTopPadding, getPaddingRight(), getPaddingBottom());

        // Ensures line height is a multiple of 4dp
        int fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading;
        float desiredLineHeight = (lineHeightHint > 0)
                ? lineHeightHint
                : lineHeightMultiplierHint * fontHeight;

        int baselineAlignedLineHeight =
                (int) (fourDip * (float) Math.ceil(desiredLineHeight / fourDip));
        setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f);
    }

    public float getLineHeightMultiplierHint() {
        return lineHeightMultiplierHint;
    }

    public void setLineHeightMultiplierHint(float lineHeightMultiplierHint) {
        this.lineHeightMultiplierHint = lineHeightMultiplierHint;
        recomputeLineHeight();
    }

    public float getLineHeightHint() {
        return lineHeightHint;
    }

    public void setLineHeightHint(float lineHeightHint) {
        this.lineHeightHint = lineHeightHint;
        recomputeLineHeight();
    }
}
