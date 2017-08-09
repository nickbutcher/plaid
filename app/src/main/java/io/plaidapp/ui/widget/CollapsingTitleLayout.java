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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.FontRes;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import io.plaidapp.R;
import io.plaidapp.ui.span.TextColorSpan;
import io.plaidapp.ui.transitions.ReflowText;
import io.plaidapp.util.CollapsingTextHelper;
import io.plaidapp.util.ViewUtils;

/**
 * A layout that draws a title and can collapse down to a condensed size.  If the title shows over
 * multiple lines then it will fade out line by line as it collapses. If the title is a single
 * line then text is initially displayed as large as possible and then scaled down to fit the within
 * the collapsed size.
 */
public class CollapsingTitleLayout extends FrameLayout implements ReflowText.Reflowable {

    // constants
    private static final int BREAK_STRATEGY = Layout.BREAK_STRATEGY_HIGH_QUALITY;

    // configurable attributes
    private int titleInsetStart;
    private float titleInsetTop;
    private int titleInsetEnd;
    private int titleInsetBottom;
    private float collapsedTextSize;
    private float maxExpandedTextSize;
    private float lineHeightHint;
    private int maxLines;

    // state
    private CharSequence title;
    private SpannableStringBuilder displayText;
    private TextPaint paint;
    private @FontRes int fontResId;
    private float textTop;
    private float scrollOffset;
    private int scrollRange;
    private float collapsedHeight;
    private CollapsingTextHelper collapsingText;
    private StaticLayout layout;
    private Line[] lines;
    private int calculatedWithWidth;
    private int lineCount;
    private float lineSpacingAdd;

    public CollapsingTitleLayout(Context context) {
        this(context, null);
    }

    public CollapsingTitleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.collapsingTitleLayoutStyle);
    }

    public CollapsingTitleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CollapsingTitleLayout(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CollapsingTitleLayout);
        final boolean isRtl = ViewCompat.getLayoutDirection(this)
                == ViewCompat.LAYOUT_DIRECTION_RTL;

        // first check if all insets set the same
        titleInsetStart = titleInsetEnd = titleInsetBottom =
                a.getDimensionPixelSize(R.styleable.CollapsingTitleLayout_titleInset, 0);
        titleInsetTop = titleInsetStart;

        if (a.hasValue(R.styleable.CollapsingTitleLayout_titleInsetStart)) {
            final int insetStart = a.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_titleInsetStart, 0);
            if (isRtl) {
                titleInsetEnd = insetStart;
            } else {
                titleInsetStart = insetStart;
            }
        }
        if (a.hasValue(R.styleable.CollapsingTitleLayout_titleInsetTop)) {
            titleInsetTop = a.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_titleInsetTop, 0);
        }
        if (a.hasValue(R.styleable.CollapsingTitleLayout_titleInsetEnd)) {
            final int insetEnd = a.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_titleInsetEnd, 0);
            if (isRtl) {
                titleInsetStart = insetEnd;
            } else {
                titleInsetEnd = insetEnd;
            }
        }
        if (a.hasValue(R.styleable.CollapsingTitleLayout_titleInsetBottom)) {
            titleInsetBottom = a.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_titleInsetBottom, 0);
        }

        // first check TextAppearance for text attributes
        if (a.hasValue(R.styleable.CollapsingTitleLayout_android_textAppearance)) {
            final int textAppearanceId = a.getResourceId(
                    R.styleable.CollapsingTitleLayout_android_textAppearance,
                    android.R.style.TextAppearance);
            TypedArray ta = getContext().obtainStyledAttributes(textAppearanceId,
                    R.styleable.CollapsingTitleLayout);
            parseTextAttrs(ta);
            ta.recycle();
        }

        // then check view for text attributes
        parseTextAttrs(a);
        a.recycle();
    }

    private void parseTextAttrs(TypedArray ta) {
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_collapsedTextSize)) {
            collapsedTextSize = ta.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_collapsedTextSize, 0);
            paint.setTextSize(collapsedTextSize);
        }
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_android_textColor)) {
            paint.setColor(ta.getColor(R.styleable.CollapsingTitleLayout_android_textColor,
                    Color.WHITE));
        }
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_android_fontFamily)) {
            try {
                fontResId =
                        ta.getResourceId(R.styleable.CollapsingTitleLayout_android_fontFamily, 0);
                Typeface font = ResourcesCompat.getFont(getContext(), fontResId);
                if (font != null) {
                    paint.setTypeface(font);
                }
            } catch (Resources.NotFoundException nfe) {
                // swallow; use default typeface
            }
        }
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_maxExpandedTextSize)) {
            maxExpandedTextSize = ta.getDimensionPixelSize(
                    R.styleable.CollapsingTitleLayout_maxExpandedTextSize, Integer.MAX_VALUE);
        }
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_lineHeightHint)) {
            lineHeightHint =
                    ta.getDimensionPixelSize(R.styleable.CollapsingTitleLayout_lineHeightHint, 0);
        }
        if (ta.hasValue(R.styleable.CollapsingTitleLayout_android_maxLines)) {
            maxLines = ta.getInteger(R.styleable.CollapsingTitleLayout_android_maxLines, 5);
        }
    }

    public void setTitle(CharSequence title) {
        this.title = title;
        this.displayText = new SpannableStringBuilder(title);
    }

    public void setScrollPixelOffset(int offset) {
        if (scrollOffset != offset) {
            scrollOffset = offset;

            if (lineCount == 1) {
                setScrollOffsetSingleLine();
            } else {
                setScrollOffsetMultiLine();
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (lineCount == 1) {
            collapsingText.draw(canvas);
        } else {
            int savePoint = canvas.save();
            float x = titleInsetStart;
            float y = Math.max(textTop - scrollOffset, titleInsetTop);
            canvas.translate(x, y);
            canvas.clipRect(0, 0,
                    getWidth() - titleInsetStart - titleInsetEnd,
                    Math.max(getHeight() - scrollOffset, collapsedHeight) - y);
            layout.draw(canvas);
            canvas.restoreToCount(savePoint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width != calculatedWithWidth) {
            recalculate(width);
        }

        final int desiredHeight = getDesiredHeight();
        int height;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case MeasureSpec.EXACTLY:
                height = MeasureSpec.getSize(heightMeasureSpec);
                break;
            case MeasureSpec.AT_MOST:
                height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                height = desiredHeight;
                break;
        }
        setMeasuredDimension(width, height);
        measureChildren(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
    }

    private int getDesiredHeight() {
        if (layout == null) return getMinimumHeight();
        return Math.max(
                (int) (titleInsetTop + layout.getHeight() + titleInsetBottom), getMinimumHeight());
    }

    private void recalculate(int width) {

        // reset stateful objects that might change over measure passes
        paint.setTextSize(collapsedTextSize);
        displayText = new SpannableStringBuilder(title);

        // Calculate line height; ensure it' a multiple of 4dp to sit on the grid
        final float fourDip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                getResources().getDisplayMetrics());
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        int fontHeight = Math.abs(fm.ascent - fm.descent) + fm.leading;
        final int baselineAlignedLineHeight =
                (int) (fourDip * (float) Math.ceil(lineHeightHint / fourDip));
        lineSpacingAdd = Math.max(0, baselineAlignedLineHeight - fontHeight);

        // now create the layout with our desired insets & line height
        createLayout(width, lineSpacingAdd);

        // adjust the title top inset to vertically center text with the toolbar
        collapsedHeight = (int) Math.max(ViewUtils.getActionBarSize(getContext()),
                (fourDip + baselineAlignedLineHeight + fourDip));
        titleInsetTop = (collapsedHeight - baselineAlignedLineHeight) / 2f;

        if (lineCount == 1) { // single line mode
            layout = null;
            collapsingText = new CollapsingTextHelper(this);
            collapsingText.setText(title);

            collapsingText.setCollapsedBounds(titleInsetStart,
                    0,
                    width - titleInsetEnd,
                    (int) collapsedHeight);

            collapsingText.setExpandedBounds(titleInsetStart,
                    (int) titleInsetTop,
                    width - titleInsetEnd,
                    getMinimumHeight() - titleInsetBottom);
            collapsingText.setCollapsedTextColor(paint.getColor());
            collapsingText.setExpandedTextColor(paint.getColor());
            collapsingText.setCollapsedTextSize(collapsedTextSize);

            int expandedTitleTextSize = (int) Math.max(collapsedTextSize,
                    ViewUtils.getSingleLineTextSize(displayText.toString(), paint,
                            width - titleInsetStart - titleInsetEnd,
                            collapsedTextSize,
                            maxExpandedTextSize, 0.5f, getResources().getDisplayMetrics()));
            collapsingText.setExpandedTextSize(expandedTitleTextSize);

            collapsingText.setExpandedTextGravity(GravityCompat.START | Gravity.BOTTOM);
            collapsingText.setCollapsedTextGravity(GravityCompat.START | Gravity.CENTER_VERTICAL);
            collapsingText.setTypeface(paint.getTypeface());

            textTop = getHeight() - titleInsetBottom - fontHeight;
            scrollRange = getMinimumHeight() - (int) collapsedHeight;
        } else { // multi-line mode
            // bottom align the text
            textTop = getDesiredHeight() - titleInsetBottom - layout.getHeight();

            // pre-calculate at what scroll offsets lines should disappear
            scrollRange = (int) (textTop - titleInsetTop);
            final int fadeDistance = layout.getLineBottom(0) - layout.getLineBaseline(0);
            lines = new Line[lineCount];
            for (int i = 1; i < lineCount; i++) {
                int lineBottomScrollOffset =
                        scrollRange + ((lineCount - i - 1) * baselineAlignedLineHeight);
                lines[i] = new Line(
                        layout.getLineStart(i),
                        layout.getLineEnd(i),
                        new TextColorSpan(paint.getColor()),
                        lineBottomScrollOffset,
                        lineBottomScrollOffset + fadeDistance);
            }
        }
        calculatedWithWidth = width;
    }

    private void createLayout(int width, float lineSpacingAdd) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createLayoutM(width, lineSpacingAdd);
        } else {
            createLayoutPreM(width, lineSpacingAdd);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void createLayoutM(int width, float lineSpacingAdd) {
        layout = StaticLayout.Builder.obtain(displayText, 0, displayText.length(), paint,
                width - titleInsetStart - titleInsetEnd)
                .setLineSpacing(lineSpacingAdd, 1f)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .setBreakStrategy(BREAK_STRATEGY)
                .build();
        lineCount = layout.getLineCount();
    }

    private void createLayoutPreM(int width, float lineSpacingAdd) {
        layout = new StaticLayout(displayText,
                paint,
                width - titleInsetStart - titleInsetEnd,
                Layout.Alignment.ALIGN_NORMAL,
                1f,
                lineSpacingAdd,
                true);
        lineCount = layout.getLineCount();

        if (lineCount > maxLines) {
            // if it exceeds our max number of lines then truncate the text & recreate the layout
            int endIndex = layout.getLineEnd(maxLines - 1) - 2; // minus 2 chars for the ellipse
            displayText = new SpannableStringBuilder(title.subSequence(0, endIndex) + "…");
            layout = new StaticLayout(displayText,
                    paint,
                    width - titleInsetStart - titleInsetEnd,
                    Layout.Alignment.ALIGN_NORMAL,
                    1f,
                    lineSpacingAdd,
                    true);
            lineCount = maxLines;
        }
    }

    private void setScrollOffsetSingleLine() {
        // see how far we have scrolled as a fraction of the scroll range
        collapsingText.setExpansionFraction(Math.min(scrollOffset, scrollRange) / scrollRange);
    }

    private void setScrollOffsetMultiLine() {
        // loop over each line and check/set an appropriate alpha for the current scroll offset
        for (int i = 1; i < lineCount; i++) {
            Line line = lines[i];
            float lineAlpha = 1f;
            if (scrollOffset >= line.zeroAlphaScrollOffset) {
                lineAlpha = 0f;
            } else if (scrollOffset <= line.fullAlphaScrollOffset) {
                lineAlpha = 1f;
            } else if (scrollOffset > line.fullAlphaScrollOffset
                    && scrollOffset < line.zeroAlphaScrollOffset) {
                lineAlpha = 1f - (scrollOffset - line.fullAlphaScrollOffset)
                        / (line.zeroAlphaScrollOffset - line.fullAlphaScrollOffset);
            }
            if (line.currentAlpha != lineAlpha) {
                // mutating a span does not re-draw, need to remove and re-set it
                displayText.removeSpan(line.span);
                line.span.setAlpha(lineAlpha);
                displayText.setSpan(line.span, line.startIndex, line.endIndex,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                line.currentAlpha = lineAlpha;
            }
        }
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public String getText() {
        return title.toString();
    }

    @Override
    public Point getTextPosition() {
        if (lineCount == 1) {
            return collapsingText.getTextTopLeft();
        } else {
            return new Point(titleInsetStart, (int) Math.max(textTop - scrollOffset, titleInsetTop));
        }
    }

    @Override
    public int getTextWidth() {
        return calculatedWithWidth - titleInsetStart - titleInsetEnd;
    }

    @Override
    public int getTextHeight() {
        return (int) (lineCount == 1 ? collapsingText.getExpandedBounds().height()
                : Math.max(getHeight() - scrollOffset, collapsedHeight));
    }

    @Override
    public float getLineSpacingAdd() {
        return lineSpacingAdd;
    }

    @Override
    public float getLineSpacingMult() {
        return 1f;
    }

    @Override
    public int getBreakStrategy() {
        return BREAK_STRATEGY;
    }

    @Override
    public float getLetterSpacing() {
        return paint.getLetterSpacing();
    }

    @Override
    public int getFontResId() {
        return fontResId;
    }

    @Override
    public float getTextSize() {
        return lineCount == 1 ? collapsingText.getCurrentTextSize() : paint.getTextSize();
    }

    @Override
    public int getTextColor() {
        return paint.getColor();
    }

    @Override
    public int getMaxLines() {
        return maxLines;
    }

    private class Line {
        int startIndex;
        int endIndex;
        TextColorSpan span;
        int fullAlphaScrollOffset;
        int zeroAlphaScrollOffset;
        float currentAlpha = 1f;

        Line(int startIndex, int endIndex, TextColorSpan span,
                    int fullAlphaScrollOffset, int zeroAlphaScrollOffset) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.span = span;
            this.zeroAlphaScrollOffset = zeroAlphaScrollOffset;
            this.fullAlphaScrollOffset = fullAlphaScrollOffset;
        }
    }

}
