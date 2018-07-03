/*
 * Copyright 2016 Google Inc.
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
import android.util.AttributeSet;
import android.util.TypedValue;

import io.plaidapp.R;
import io.plaidapp.core.ui.widget.BaselineGridTextView;
import io.plaidapp.core.util.ViewUtils;

/**
 * An extension to {@link android.widget.TextView} which sizes text to grow up to a specified
 * maximum size, per the material spec:
 * https://www.google.com/design/spec/style/typography.html#typography-other-typographic-guidelines
 */
public class DynamicTypeTextView extends BaselineGridTextView {

    // configurable attributes
    private final float minTextSize;
    private final float maxTextSize;

    public DynamicTypeTextView(Context context) {
        this(context, null);
    }

    public DynamicTypeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public DynamicTypeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        /* re-use CollapsingTitleLayout attribs */
        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.CollapsingTitleLayout);
        if (a.hasValue(R.styleable.CollapsingTitleLayout_collapsedTextSize)) {
            minTextSize = a.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_collapsedTextSize, 0);
            setTextSize(TypedValue.COMPLEX_UNIT_PX, minTextSize);
        } else {
            // if not explicitly set then use the default text size as the min
            minTextSize = getTextSize();
        }
        maxTextSize = a.getDimensionPixelSize(
                R.styleable.CollapsingTitleLayout_maxExpandedTextSize, Integer.MAX_VALUE);
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        final float expandedTitleTextSize = Math.max(minTextSize,
                ViewUtils.getSingleLineTextSize(getText().toString(), getPaint(),
                        w - getPaddingStart() - getPaddingEnd(),
                        minTextSize,
                        maxTextSize, 0.5f, getResources().getDisplayMetrics()));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, expandedTitleTextSize);
    }
}
