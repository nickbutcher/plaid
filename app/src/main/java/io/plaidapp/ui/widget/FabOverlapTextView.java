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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.view.GravityCompat;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import in.uncod.android.bypass.style.TouchableUrlSpan;
import io.plaidapp.R;
import io.plaidapp.util.FontUtil;

/**
 * A view for displaying text that is will be overlapped by a Floating Action Button (FAB).
 * This view will indent itself at the given overlap point (as specified by
 * {@link #setFabOverlapGravity(int)}) to flow around it.
 *
 * Not actually a TextView but conforms to many of it's idioms.
 */
@TargetApi(Build.VERSION_CODES.M)
public class FabOverlapTextView extends View {

    private static final int DEFAULT_TEXT_SIZE_SP = 14;

    private int fabOverlapHeight;
    private int fabOverlapWidth;
    private int fabGravity;
    private int lineHeightHint;
    private int topPaddingHint;
    private int breakStrategy;
    private StaticLayout layout;
    private CharSequence text;
    private TextPaint paint;
    private TouchableUrlSpan pressedSpan;

    public FabOverlapTextView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public FabOverlapTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public FabOverlapTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public FabOverlapTextView(Context context, AttributeSet attrs, int defStyleAttr, int
            defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FabOverlapTextView);

        float defaultTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SIZE_SP, getResources().getDisplayMetrics());

        setFabOverlapGravity(a.getInt(R.styleable.FabOverlapTextView_fabGravity,
                Gravity.BOTTOM | Gravity.RIGHT));
        setFabOverlapHeight(a.getDimensionPixelSize(R.styleable
                .FabOverlapTextView_fabOverlayHeight, 0));
        setFabOverlapWidth(a.getDimensionPixelSize(R.styleable
                .FabOverlapTextView_fabOverlayWidth, 0));

        if (a.hasValue(R.styleable.FabOverlapTextView_android_textAppearance)) {
            final int textAppearance = a.getResourceId(
                    R.styleable.FabOverlapTextView_android_textAppearance,
                    android.R.style.TextAppearance);
            TypedArray atp = getContext().obtainStyledAttributes(textAppearance,
                    R.styleable.FontTextAppearance);
            paint.setColor(atp.getColor(R.styleable.FontTextAppearance_android_textColor,
                    Color.BLACK));
            paint.setTextSize(atp.getDimensionPixelSize(
                    R.styleable.FontTextAppearance_android_textSize, (int) defaultTextSize));
            if (atp.hasValue(R.styleable.FontTextAppearance_font)) {
                paint.setTypeface(FontUtil.get(getContext(),
                        atp.getString(R.styleable.FontTextAppearance_font)));
            }
            atp.recycle();
        }

        if (a.hasValue(R.styleable.FabOverlapTextView_font)) {
            setFont(a.getString(R.styleable.FabOverlapTextView_font));
        }

        if (a.hasValue(R.styleable.FabOverlapTextView_android_textColor)) {
            setTextColor(a.getColor(R.styleable.FabOverlapTextView_android_textColor, 0));
        }
        if (a.hasValue(R.styleable.FabOverlapTextView_android_textSize)) {
            setTextSize(a.getDimensionPixelSize(R.styleable.FabOverlapTextView_android_textSize,
                    (int) defaultTextSize));
        }

        lineHeightHint = a.getDimensionPixelSize(R.styleable.FabOverlapTextView_lineHeightHint, 0);
        topPaddingHint = a.getDimensionPixelSize(R.styleable.FabOverlapTextView_topPaddingHint, 0);

        breakStrategy = a.getInt(R.styleable.FabOverlapTextView_android_breakStrategy,
                Layout.BREAK_STRATEGY_BALANCED);

        a.recycle();
    }

    public void setFabOverlapGravity(int fabGravity) {
        // we only really support [top|bottom][left|right|start|end]
        // TODO validate input
        this.fabGravity = GravityCompat.getAbsoluteGravity(fabGravity, getLayoutDirection());
    }

    public void setFabOverlapHeight(int fabOverlapHeight) {
        this.fabOverlapHeight = fabOverlapHeight;
    }

    public void setFabOverlapWidth(int fabOverlapWidth) {
        this.fabOverlapWidth = fabOverlapWidth;
    }

    public void setText(CharSequence text) {
        this.text = text;
        layout = null;
        recompute(getWidth());
        requestLayout();
    }

    public void setTextSize(int textSize) {
        paint.setTextSize(textSize);
    }

    public void setTextColor(@ColorInt int color) {
        paint.setColor(color);
    }

    public void setTypeface(Typeface typeface) {
        paint.setTypeface(typeface);
    }

    public void setFont(String font) {
        setTypeface(FontUtil.get(getContext(), font));
    }

    public void setLetterSpacing(float letterSpacing) {
        paint.setLetterSpacing(letterSpacing);
    }

    public void setFontFeatureSettings(String fontFeatureSettings) {
        paint.setFontFeatureSettings(fontFeatureSettings);
    }

    private void recompute(int width) {
        if (text != null) {
            // work out the top padding and line height to align text to a 4dp grid
            float fourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                    getResources().getDisplayMetrics());

            // Ensure that the first line's baselines sits on 4dp grid by setting the top padding
            Paint.FontMetricsInt fm = paint.getFontMetricsInt();
            int gridAlignedTopPadding = (int) (fourDip * (float)
                    Math.ceil((topPaddingHint + Math.abs(fm.ascent)) / fourDip)
                    - Math.ceil(Math.abs(fm.ascent)));
            setPadding(getPaddingLeft(), gridAlignedTopPadding, getPaddingRight(),
                    getPaddingBottom());

            // Ensures line height is a multiple of 4dp
            int fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading;
            int baselineAlignedLineHeight =
                    (int) (fourDip * (float) Math.ceil(lineHeightHint / fourDip));

            // before we can workout indents we need to know how many lines of text there are;
            // so we need to create a temporary layout :(
            layout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                    .setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f)
                    .setBreakStrategy(breakStrategy)
                    .build();
            int preIndentedLineCount = layout.getLineCount();

            // now we can calculate the indents required for the given fab gravity
            boolean gravityTop = (fabGravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP;
            boolean gravityLeft = (fabGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT;
            // we want to iterate forward/backward over the lines depending on whether the fab
            // overlap vertical gravity is top/bottom
            int currentLine = gravityTop ? 0 : preIndentedLineCount - 1;
            int remainingHeightOverlap = fabOverlapHeight -
                    (gravityTop ? getPaddingTop() : getPaddingBottom());
            int[] leftIndents = new int[preIndentedLineCount];
            int[] rightIndents = new int[preIndentedLineCount];
            do {
                if (remainingHeightOverlap > 0) {
                    // still have overlap height to consume, set the appropriate indent
                    leftIndents[currentLine] = gravityLeft ? fabOverlapWidth : 0;
                    rightIndents[currentLine] = gravityLeft ? 0 : fabOverlapWidth;
                    remainingHeightOverlap -= baselineAlignedLineHeight;
                } else {
                    // have consumed the overlap height: no indent
                    leftIndents[currentLine] = 0;
                    rightIndents[currentLine] = 0;
                }
                if (gravityTop) { // iterate forward over the lines
                    currentLine++;
                } else { // iterate backward over the lines
                    currentLine--;
                }
            } while (gravityTop ? currentLine < preIndentedLineCount : currentLine >= 0);

            // now that we know the indents, create the actual layout
            layout = StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
                    .setLineSpacing(baselineAlignedLineHeight - fontHeight, 1f)
                    .setIndents(leftIndents, rightIndents)
                    .setBreakStrategy(breakStrategy)
                    .build();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            throw new IllegalArgumentException("FabOverlapTextView requires a constrained width");
        }
        int layoutWidth = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() -
                getPaddingRight();
        if (layout == null || layoutWidth != layout.getWidth()) {
            recompute(layoutWidth);
        }
        setMeasuredDimension(
                getPaddingLeft() + (layout != null ? layout.getWidth() : 0) + getPaddingRight(),
                getPaddingTop() + (layout != null ? layout.getHeight() : 0) + getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (layout != null) {
            canvas.translate(getPaddingLeft(), getPaddingTop());
            layout.draw(canvas);
        }
    }

    /**
     * This is why you don't implement your own TextView kids; you have to handle everything!
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!(text instanceof Spanned)) return super.onTouchEvent(event);

        Spannable spannedText = (Spannable) text;

        boolean handled = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedSpan = getPressedSpan(spannedText, event);
            if (pressedSpan != null) {
                pressedSpan.setPressed(true);
                Selection.setSelection(spannedText, spannedText.getSpanStart(pressedSpan),
                        spannedText.getSpanEnd(pressedSpan));
                handled = true;
                postInvalidateOnAnimation();
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            TouchableUrlSpan touchedSpan = getPressedSpan(spannedText, event);
            if (pressedSpan != null && touchedSpan != pressedSpan) {
                pressedSpan.setPressed(false);
                pressedSpan = null;
                Selection.removeSelection(spannedText);
                postInvalidateOnAnimation();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pressedSpan != null) {
                pressedSpan.setPressed(false);
                pressedSpan.onClick(this);
                handled = true;
                postInvalidateOnAnimation();
            }
            pressedSpan = null;
            Selection.removeSelection(spannedText);
        } else {
            if (pressedSpan != null) {
                pressedSpan.setPressed(false);
                handled = true;
                postInvalidateOnAnimation();
            }
            pressedSpan = null;
            Selection.removeSelection(spannedText);
        }
        return handled;
    }

    private TouchableUrlSpan getPressedSpan(Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= getPaddingLeft();
        y -= getPaddingTop();

        x += getScrollX();
        y += getScrollY();

        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        TouchableUrlSpan[] link = spannable.getSpans(off, off, TouchableUrlSpan.class);
        TouchableUrlSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

}
