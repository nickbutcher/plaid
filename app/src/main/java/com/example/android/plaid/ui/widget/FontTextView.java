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

package com.example.android.plaid.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.example.android.plaid.R;
import com.example.android.plaid.ui.util.FontUtil;

/**
 * Created by nickbutcher on 12/17/14.
 */
public class FontTextView extends TextView {


    public FontTextView(Context context) {
        super(context);
        init(context, null);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);

        if (a.hasValue(R.styleable.FontTextView_android_textAppearance)) {
            final int textAppearanceId = a.getResourceId(R.styleable
                            .FontTextView_android_textAppearance,
                    android.R.style.TextAppearance);
            TypedArray atp = getContext().obtainStyledAttributes(textAppearanceId,
                    R.styleable.FontTextAppearance);
            if (atp.hasValue(R.styleable.FontTextAppearance_android_textColor)) {
                setTextColor(atp.getColor(
                        R.styleable.FontTextAppearance_android_textColor, Color.WHITE));
            }
            if (atp.hasValue(R.styleable.FontTextAppearance_android_textSize)) {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, atp.getDimensionPixelSize(
                        R.styleable.FontTextAppearance_android_textSize, 0));
            }
            if (atp.hasValue(R.styleable.FontTextAppearance_font)) {
                setFont(atp.getString(R.styleable.FontTextAppearance_font));
            }
            atp.recycle();
        }

        if (a.hasValue(R.styleable.FontTextView_font)) {
            setFont(a.getString(R.styleable.FontTextView_font));
        }
        a.recycle();
    }

    public void setFont(String font) {
        setPaintFlags(getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        setTypeface(FontUtil.get(getContext(), font));
    }
}
