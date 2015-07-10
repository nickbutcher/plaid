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
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import io.plaidapp.R;

/**
 * TODO: document your custom view class.
 */
public class DynamicTextView extends TextView {

    private static MaterialTypeStyle[] mStyles = {
            new MaterialTypeStyle(112, "sans-serif-light", 0x8a),    /* Display 4 */
            new MaterialTypeStyle(56, "sans-serif", 0x8a),    /* Display 3 */
            new MaterialTypeStyle(45, "sans-serif", 0x8a),    /* Display 2 */
            new MaterialTypeStyle(34, "sans-serif", 0x8a),    /* Display 1 */
            new MaterialTypeStyle(24, "sans-serif", 0xde),    /* Headline */
            new MaterialTypeStyle(20, "sans-serif-medium", 0xde)     /* Title */
    };
    private boolean mSnapToMaterialScale;
    private int mMinTextSize;
    private int mMaxTextSize;
    private float scaledDensity;
    private boolean mCalculated = false;

    public DynamicTextView(Context context) {
        super(context);
        init(null, 0);
    }

    public DynamicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public DynamicTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        scaledDensity = getContext().getResources().getDisplayMetrics().scaledDensity;
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DynamicTextView, defStyle, 0);

        mSnapToMaterialScale = a.getBoolean(R.styleable.DynamicTextView_snapToMaterialScale, true);
        mMinTextSize = a.getDimensionPixelSize(
                R.styleable.DynamicTextView_minTextSize,
                (int) (20 * scaledDensity));

        mMaxTextSize = a.getDimensionPixelSize(
                R.styleable.DynamicTextView_maxTextSize,
                (int) (112 * scaledDensity));

        a.recycle();
    }

    private void fitText() {

        // different methods for achieving this depending on whether we are snapping to the material
        // scale, and if multiple lines are allowed.  4 method for the permutations of this.

        if (mSnapToMaterialScale && getMaxLines() == 1) {
            // technically we could use the multi line algorithm here but this is more efficient
            fitSnappedSingleLine();
        } else if (mSnapToMaterialScale) {
            fitSnappedMultiLine();
        } else if (!mSnapToMaterialScale && getMaxLines() == 1) {
            fitSingleLine();
        } else if (!mSnapToMaterialScale) {
            fitMultiline();
        }
    }

    private void fitSnappedMultiLine() {
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int targetHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (targetWidth > 0 && targetHeight > 0) {
            int style = 0;
            MaterialTypeStyle currentStyle = mStyles[style];
            TextPaint paint = getPaint();
            StaticLayout staticLayout = null;
            int currentHeight = Integer.MAX_VALUE;
            int lines = 0;
            boolean maxLinesSet = getMaxLines() != Integer.MAX_VALUE;

            while ((currentHeight > targetHeight || (maxLinesSet && lines > getMaxLines()))
                    && style <= mStyles.length - 1
                    && currentStyle.size * scaledDensity >= mMinTextSize
                    && currentStyle.size * scaledDensity <= mMaxTextSize) {
                currentStyle = mStyles[style];
                paint.setTextSize(currentStyle.size * scaledDensity);
                paint.setTypeface(Typeface.create(currentStyle.fontFamily, Typeface.NORMAL));
                staticLayout = new StaticLayout(getText(), paint, targetWidth, Layout.Alignment
                        .ALIGN_NORMAL, 1.0f, 0.0f, true);
                currentHeight = staticLayout.getHeight();
                lines = staticLayout.getLineCount();
                style++;
            }
            super.setTextSize(TypedValue.COMPLEX_UNIT_SP, currentStyle.size);
            setTypeface(Typeface.create(currentStyle.fontFamily, Typeface.NORMAL));

            int currentColour = getCurrentTextColor();
            setTextColor(Color.argb(currentStyle.opacity,
                    Color.red(currentColour),
                    Color.green(currentColour),
                    Color.blue(currentColour)));

            if (style == mStyles.length) {
                setEllipsize(TextUtils.TruncateAt.END);
            }
            if (currentStyle.size * scaledDensity < mMinTextSize) {
                // wanted to make text smaller but hit min text size.  Need to set max lines.
                setMaxLines((int) Math.floor((((float) targetHeight / (float) currentHeight) *
                        lines)));
                setEllipsize(TextUtils.TruncateAt.END);
            }
            setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
            mCalculated = true;
        }
    }

    private void fitSnappedSingleLine() {
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (targetWidth > 0) {
            int style = 0;
            TextPaint paint = getPaint();
            final String text = getText().toString();
            MaterialTypeStyle currentStyle = null;
            float currentWidth = Float.MAX_VALUE;

            while (currentWidth > targetWidth && style < mStyles.length) {
                currentStyle = mStyles[style];
                paint.setTextSize(currentStyle.size * scaledDensity);
                paint.setTypeface(Typeface.create(currentStyle.fontFamily, Typeface.NORMAL));
                currentWidth = paint.measureText(text);
                style++;
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, currentStyle.size);
            setTypeface(Typeface.create(currentStyle.fontFamily, Typeface.NORMAL));

            int currentColour = getCurrentTextColor();
            setTextColor(Color.argb(currentStyle.opacity,
                    Color.red(currentColour),
                    Color.green(currentColour),
                    Color.blue(currentColour)));

            if (style == mStyles.length) {
                setEllipsize(TextUtils.TruncateAt.END);
            }
            mCalculated = true;
        }
    }

    private void fitMultiline() {
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int targetHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (targetWidth > 0 && targetHeight > 0) {
            int textSize = mMaxTextSize;
            TextPaint paint = getPaint();
            paint.setTextSize(textSize);
            StaticLayout staticLayout = new StaticLayout(getText(), paint, targetWidth, Layout
                    .Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
            int currentHeight = staticLayout.getHeight();

            while (currentHeight > targetHeight && textSize > mMinTextSize) {
                textSize--;
                paint.setTextSize(textSize);
                staticLayout = new StaticLayout(getText(), paint, targetWidth, Layout.Alignment
                        .ALIGN_NORMAL, 1.0f, 0.0f, true);
                currentHeight = staticLayout.getHeight();
            }
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            setTextAlignment(TEXT_ALIGNMENT_TEXT_START);
            mCalculated = true;
        }
    }

    private void fitSingleLine() {
        int targetWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (targetWidth > 0) {
            int textSize = mMaxTextSize;
            TextPaint paint = getPaint();
            paint.setTextSize(textSize);
            final String text = getText().toString();
            float currentWidth = paint.measureText(text);

            while (currentWidth > targetWidth && textSize > mMinTextSize) {
                textSize--;
                paint.setTextSize(textSize);
                currentWidth = paint.measureText(text);
            }
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mCalculated = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!mCalculated) {
            fitText();
        }
    }

    public boolean isSnapToMaterialScale() {
        return mSnapToMaterialScale;
    }

    public void setSnapToMaterialScale(boolean snapToMaterialScale) {
        this.mSnapToMaterialScale = snapToMaterialScale;
    }

    public int getMinTextSize() {
        return mMinTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        this.mMinTextSize = minTextSize;
    }

    public int getMaxTextSize() {
        return mMaxTextSize;
    }

    public void setMaxTextSize(int maxTextSize) {
        this.mMaxTextSize = maxTextSize;
    }

    private static class MaterialTypeStyle {
        int size;
        String fontFamily;
        int opacity;

        MaterialTypeStyle(int size, String fontFamily, int opacity) {
            this.size = size;
            this.fontFamily = fontFamily;
            this.opacity = opacity;
        }
    }
}
