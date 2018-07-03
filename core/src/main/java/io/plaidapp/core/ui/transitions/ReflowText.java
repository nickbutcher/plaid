/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.FontRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.core.R;
import io.plaidapp.core.ui.widget.BaselineGridTextView;

/**
 * A transition for repositioning text. This will animate changes in text size and position,
 * re-flowing line breaks as necessary.
 * <p>
 * Strongly recommended to use a curved {@code pathMotion} for a more natural transition.
 */
public class ReflowText extends Transition {

    private static final String EXTRA_REFLOW_DATA = "EXTRA_REFLOW_DATA";
    private static final String PROPNAME_DATA = "plaid:reflowtext:data";
    private static final String PROPNAME_TEXT_SIZE = "plaid:reflowtext:textsize";
    private static final String PROPNAME_BOUNDS = "plaid:reflowtext:bounds";
    private static final String[] PROPERTIES = { PROPNAME_TEXT_SIZE, PROPNAME_BOUNDS };
    private static final int TRANSPARENT = 0;
    private static final int OPAQUE = 255;
    private static final int OPACITY_MID_TRANSITION = (int) (0.8f * OPAQUE);
    private static final float STAGGER_DECAY = 0.8f;

    private int velocity = 700;         // pixels per second
    private long minDuration = 200;     // ms
    private long maxDuration = 400;     // ms
    private long staggerDelay = 40;     // ms
    private long duration;
    // this is hack for preventing view from drawing briefly at the end of the transition :(
    private final boolean freezeFrame;

    public ReflowText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ReflowText);
        velocity = a.getDimensionPixelSize(R.styleable.ReflowText_velocity, velocity);
        minDuration = a.getInt(R.styleable.ReflowText_minDuration, (int) minDuration);
        maxDuration = a.getInt(R.styleable.ReflowText_maxDuration, (int) maxDuration);
        staggerDelay = a.getInt(R.styleable.ReflowText_staggerDelay, (int) staggerDelay);
        freezeFrame = a.getBoolean(R.styleable.ReflowText_freezeFrame, false);
        a.recycle();
    }

    /**
     * Store data about the view which will participate in a reflow transition in {@code intent}.
     */
    public static void addExtras(@NonNull Intent intent, @NonNull Reflowable reflowableView) {
        intent.putExtra(EXTRA_REFLOW_DATA, new ReflowData(reflowableView));
    }

    /**
     * Retrieve data about the reflow from {@code intent} and store it for later use.
     */
    public static void setupReflow(@NonNull Intent intent, @NonNull View view) {
        view.setTag(R.id.tag_reflow_data, intent.getParcelableExtra(EXTRA_REFLOW_DATA));
    }

    /**
     * Create data about the reflow from {@code reflowableView} and store it for later use.
     */
    public static void setupReflow(@NonNull Reflowable reflowableView) {
        reflowableView.getView().setTag(R.id.tag_reflow_data, new ReflowData(reflowableView));
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public String[] getTransitionProperties() {
        return PROPERTIES;
    }

    @Override
    public Animator createAnimator(
            ViewGroup sceneRoot,
            TransitionValues startValues,
            TransitionValues endValues) {

        if (startValues == null || endValues == null) return null;

        final View view = endValues.view;
        AnimatorSet transition = new AnimatorSet();
        ReflowData startData = (ReflowData) startValues.values.get(PROPNAME_DATA);
        ReflowData endData = (ReflowData) endValues.values.get(PROPNAME_DATA);
        duration = calculateDuration(startData.bounds, endData.bounds);

        // create layouts & capture a bitmaps of the text in both states
        // (with max lines variants where needed)
        Layout startLayout = createLayout(startData, sceneRoot.getContext(), false);
        Layout endLayout = createLayout(endData, sceneRoot.getContext(), false);
        Layout startLayoutMaxLines = null;
        Layout endLayoutMaxLines = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // StaticLayout maxLines support
            if (startData.maxLines != -1) {
                startLayoutMaxLines = createLayout(startData, sceneRoot.getContext(), true);
            }
            if (endData.maxLines != -1) {
                endLayoutMaxLines = createLayout(endData, sceneRoot.getContext(), true);
            }
        }
        final Bitmap startText = createBitmap(startData,
                startLayoutMaxLines != null ? startLayoutMaxLines : startLayout);
        final Bitmap endText = createBitmap(endData,
                endLayoutMaxLines != null ? endLayoutMaxLines : endLayout);

        // temporarily turn off clipping so we can draw outside of our bounds don't draw
        view.setWillNotDraw(true);
        ((ViewGroup) view.getParent()).setClipChildren(false);

        // calculate the runs of text to move together
        List<Run> runs = getRuns(startData, startLayout, startLayoutMaxLines,
                endData, endLayout, endLayoutMaxLines);

        // create animators for moving, scaling and fading each run of text
        transition.playTogether(
                createRunAnimators(view, startData, endData, startText, endText, runs));

        if (!freezeFrame) {
            transition.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    // clean up
                    view.setWillNotDraw(false);
                    view.getOverlay().clear();
                    ((ViewGroup) view.getParent()).setClipChildren(true);
                    startText.recycle();
                    endText.recycle();
                }
            });
        }
        return transition;
    }

    @Override
    public Transition setDuration(long duration) {
        /* don't call super as we want to handle duration ourselves */
        return this;
    }

    private void captureValues(TransitionValues transitionValues) {
        ReflowData reflowData = getReflowData(transitionValues.view);
        transitionValues.values.put(PROPNAME_DATA, reflowData);
        if (reflowData != null) {
            // add these props to the map separately (even though they are captured in the reflow
            // data) to use only them to determine whether to create an animation i.e. only
            // animate if text size or bounds have changed (see #getTransitionProperties())
            transitionValues.values.put(PROPNAME_TEXT_SIZE, reflowData.textSize);
            transitionValues.values.put(PROPNAME_BOUNDS, reflowData.bounds);
        }
    }

    private ReflowData getReflowData(@NonNull View view) {
        ReflowData reflowData = (ReflowData) view.getTag(R.id.tag_reflow_data);
        if (reflowData != null) {
            view.setTag(R.id.tag_reflow_data, null);
            return reflowData;
        }
        return null;
    }

    /**
     * Calculate the {@linkplain Run}s i.e. diff the start and end states, see where text changes
     * line and track the bounds of sections of text that can move together.
     * <p>
     * If a text block has a max number of lines, consider both with and without this limit applied.
     * This allows simulating the correct line breaking as well as calculating the position that
     * overflowing text would have been laid out, so that it can animate from/to that position.
     */
    private List<Run> getRuns(@NonNull ReflowData startData, @NonNull Layout startLayout,
                              @Nullable Layout startLayoutMaxLines, @NonNull ReflowData endData,
                              @NonNull Layout endLayout, @Nullable Layout endLayoutMaxLines) {
        int textLength = endLayout.getText().length();
        int currentStartLine = 0;
        int currentStartRunLeft = 0;
        int currentStartRunTop = 0;
        int currentEndLine = 0;
        int currentEndRunLeft = 0;
        int currentEndRunTop = 0;
        List<Run> runs = new ArrayList<>(endLayout.getLineCount());

        for (int i = 0; i < textLength; i++) {
            // work out which line this letter is on in the start state
            int startLine = -1;
            boolean startMax = false;
            boolean startMaxEllipsis = false;
            if (startLayoutMaxLines != null) {
                char letter = startLayoutMaxLines.getText().charAt(i);
                startMaxEllipsis = letter == '…';
                if (letter != '\uFEFF'              // beyond max lines
                        && !startMaxEllipsis) {     // ellipsize inserted into layout
                    startLine = startLayoutMaxLines.getLineForOffset(i);
                    startMax = true;
                }
            }
            if (!startMax) {
                startLine = startLayout.getLineForOffset(i);
            }

            // work out which line this letter is on in the end state
            int endLine = -1;
            boolean endMax = false;
            boolean endMaxEllipsis = false;
            if (endLayoutMaxLines != null) {
                char letter = endLayoutMaxLines.getText().charAt(i);
                endMaxEllipsis = letter == '…';
                if (letter != '\uFEFF'              // beyond max lines
                        && !endMaxEllipsis) {       // ellipsize inserted into layout
                    endLine = endLayoutMaxLines.getLineForOffset(i);
                    endMax = true;
                }
            }
            if (!endMax) {
                endLine = endLayout.getLineForOffset(i);
            }
            boolean lastChar = i == textLength - 1;

            if (startLine != currentStartLine
                    || endLine != currentEndLine
                    || lastChar) {
                // at a run boundary, store bounds in both states
                int startRunRight = getRunRight(startLayout, startLayoutMaxLines,
                        currentStartLine, i, startLine, startMax, startMaxEllipsis, lastChar);
                int startRunBottom = startLayout.getLineBottom(currentStartLine);
                int endRunRight = getRunRight(endLayout, endLayoutMaxLines, currentEndLine, i,
                        endLine, endMax, endMaxEllipsis, lastChar);
                int endRunBottom = endLayout.getLineBottom(currentEndLine);

                Rect startBound = new Rect(
                        currentStartRunLeft, currentStartRunTop, startRunRight, startRunBottom);
                startBound.offset(startData.textPosition.x, startData.textPosition.y);
                Rect endBound = new Rect(
                        currentEndRunLeft, currentEndRunTop, endRunRight, endRunBottom);
                endBound.offset(endData.textPosition.x, endData.textPosition.y);
                runs.add(new Run(
                        startBound,
                        startMax || startRunBottom <= startData.textHeight,
                        endBound,
                        endMax || endRunBottom <= endData.textHeight));
                currentStartLine = startLine;
                currentStartRunLeft = (int) (startMax ? startLayoutMaxLines
                        .getPrimaryHorizontal(i) : startLayout.getPrimaryHorizontal(i));
                currentStartRunTop = startLayout.getLineTop(startLine);
                currentEndLine = endLine;
                currentEndRunLeft = (int) (endMax ? endLayoutMaxLines
                        .getPrimaryHorizontal(i) : endLayout.getPrimaryHorizontal(i));
                currentEndRunTop = endLayout.getLineTop(endLine);
            }
        }
        return runs;
    }

    /**
     * Calculate the right boundary for this run (harder than it sounds). As we're a letter ahead,
     * need to grab either current letter start or the end of the previous line. Also need to
     * consider maxLines case, which inserts ellipses at the overflow point – don't include these.
     */
    private int getRunRight(
            Layout unrestrictedLayout, Layout maxLinesLayout, int currentLine, int index,
            int line, boolean withinMax, boolean isMaxEllipsis, boolean isLastChar) {
        int runRight;
        if (line != currentLine || isLastChar) {
            if (isMaxEllipsis) {
                runRight = (int) maxLinesLayout.getPrimaryHorizontal(index);
            } else {
                runRight = (int) unrestrictedLayout.getLineMax(currentLine);
            }
        } else {
            if (withinMax) {
                runRight = (int) maxLinesLayout.getPrimaryHorizontal(index);
            } else {
                runRight = (int) unrestrictedLayout.getPrimaryHorizontal(index);
            }
        }
        return runRight;
    }

    /**
     * Create Animators to transition each run of text from start to end position and size.
     */
    @NonNull
    private List<Animator> createRunAnimators(
            View view,
            ReflowData startData,
            ReflowData endData,
            Bitmap startText,
            Bitmap endText,
            List<Run> runs) {
        List<Animator> animators = new ArrayList<>(runs.size());
        int dx = startData.bounds.left - endData.bounds.left;
        int dy = startData.bounds.top - endData.bounds.top;
        long startDelay = 0L;
        // move text closest to the destination first i.e. loop forward or backward over the runs
        boolean upward = startData.bounds.centerY() > endData.bounds.centerY();
        boolean first = true;
        boolean lastRightward = true;
        LinearInterpolator linearInterpolator = new LinearInterpolator();

        for (int i = upward ? 0 : runs.size() - 1;
             ((upward && i < runs.size()) || (!upward && i >= 0));
             i += (upward ? 1 : -1)) {
            Run run = runs.get(i);

            // skip text runs which aren't visible in either state
            if (!run.startVisible && !run.endVisible) continue;

            // create & position the drawable which displays the run; add it to the overlay.
            SwitchDrawable drawable = new SwitchDrawable(
                    startText, run.start, startData.textSize,
                    endText, run.end, endData.textSize);
            drawable.setBounds(
                    run.start.left + dx,
                    run.start.top + dy,
                    run.start.right + dx,
                    run.start.bottom + dy);
            view.getOverlay().add(drawable);

            PropertyValuesHolder topLeft =
                    PropertyValuesHolder.ofObject(SwitchDrawable.TOP_LEFT, null,
                            getPathMotion().getPath(
                                    run.start.left + dx,
                                    run.start.top + dy,
                                    run.end.left,
                                    run.end.top));
            PropertyValuesHolder width = PropertyValuesHolder.ofInt(
                    SwitchDrawable.WIDTH, run.start.width(), run.end.width());
            PropertyValuesHolder height = PropertyValuesHolder.ofInt(
                    SwitchDrawable.HEIGHT, run.start.height(), run.end.height());
            // the progress property drives the switching behaviour
            PropertyValuesHolder progress = PropertyValuesHolder.ofFloat(
                    SwitchDrawable.PROGRESS, 0f, 1f);
            Animator runAnim = ObjectAnimator.ofPropertyValuesHolder(
                    drawable, topLeft, width, height, progress);

            boolean rightward = run.start.centerX() + dx < run.end.centerX();
            if ((run.startVisible && run.endVisible)
                    && !first && rightward != lastRightward) {
                // increase the start delay (by a decreasing amount) for the next run
                // (if it's visible throughout) to stagger the movement and try to minimize overlaps
                startDelay += staggerDelay;
                staggerDelay *= STAGGER_DECAY;
            }
            lastRightward = rightward;
            first = false;

            runAnim.setStartDelay(startDelay);
            long animDuration = Math.max(minDuration, duration - (startDelay / 2));
            runAnim.setDuration(animDuration);
            animators.add(runAnim);

            if (run.startVisible != run.endVisible) {
                // if run is appearing/disappearing then fade it in/out
                ObjectAnimator fade = ObjectAnimator.ofInt(
                        drawable,
                        SwitchDrawable.ALPHA,
                        run.startVisible ? OPAQUE : TRANSPARENT,
                        run.endVisible ? OPAQUE : TRANSPARENT);
                fade.setDuration((duration + startDelay) / 2);
                if (!run.startVisible) {
                    drawable.setAlpha(TRANSPARENT);
                    fade.setStartDelay((duration + startDelay) / 2);
                } else {
                    fade.setStartDelay(startDelay);
                }
                animators.add(fade);
            } else {
                // slightly fade during transition to minimize movement
                ObjectAnimator fade = ObjectAnimator.ofInt(
                        drawable,
                        SwitchDrawable.ALPHA,
                        OPAQUE, OPACITY_MID_TRANSITION, OPAQUE);
                fade.setStartDelay(startDelay);
                fade.setDuration(duration + startDelay);
                fade.setInterpolator(linearInterpolator);
                animators.add(fade);
            }
        }
        return animators;
    }

    private Layout createLayout(ReflowData data, Context context, boolean enforceMaxLines) {
        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(data.textSize);
        paint.setColor(data.textColor);
        paint.setLetterSpacing(data.letterSpacing);
        if (data.fontResId != 0) {
            try {
                Typeface font = ResourcesCompat.getFont(context, data.fontResId);
                if (font != null) {
                    paint.setTypeface(font);
                }
            } catch (Resources.NotFoundException nfe) { }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder builder =  StaticLayout.Builder.obtain(
                    data.text, 0, data.text.length(), paint, data.textWidth)
                    .setLineSpacing(data.lineSpacingAdd, data.lineSpacingMult)
                    .setBreakStrategy(data.breakStrategy);
            if (enforceMaxLines && data.maxLines != -1) {
                builder.setMaxLines(data.maxLines);
                builder.setEllipsize(TextUtils.TruncateAt.END);
            }
            return builder.build();
        } else {
            return new StaticLayout(
                    data.text,
                    paint,
                    data.textWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    data.lineSpacingMult,
                    data.lineSpacingAdd,
                    true);
        }
    }

    private Bitmap createBitmap(@NonNull ReflowData data, @NonNull Layout layout) {
        Bitmap bitmap = Bitmap.createBitmap(
                data.bounds.width(), data.bounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(data.textPosition.x, data.textPosition.y);
        layout.draw(canvas);
        return bitmap;
    }

    /**
     * Calculate the duration for the transition depending upon how far the text has to move.
     */
    private long calculateDuration(@NonNull Rect startPosition, @NonNull Rect endPosition) {
        float distance = (float) Math.hypot(
                startPosition.exactCenterX() - endPosition.exactCenterX(),
                startPosition.exactCenterY() - endPosition.exactCenterY());
        long duration = (long) (1000 * (distance / velocity));
        return Math.max(minDuration, Math.min(maxDuration, duration));
    }

    /**
     * Holds all data needed to describe a block of text i.e. to be able to re-create the
     * {@link Layout}.
     */
    private static class ReflowData implements Parcelable {

        final String text;
        final float textSize;
        final @ColorInt int textColor;
        final Rect bounds;
        final @FontRes int fontResId;
        final float lineSpacingAdd;
        final float lineSpacingMult;
        final Point textPosition;
        final int textHeight;
        final int textWidth;
        final int breakStrategy;
        final float letterSpacing;
        final int maxLines;

        ReflowData(@NonNull Reflowable reflowable) {
            text = reflowable.getText();
            textSize = reflowable.getTextSize();
            textColor = reflowable.getTextColor();
            fontResId = reflowable.getFontResId();
            final View view = reflowable.getView();
            int[] loc = new int[2];
            view.getLocationInWindow(loc);
            bounds = new Rect(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
            textPosition = reflowable.getTextPosition();
            textHeight = reflowable.getTextHeight();
            lineSpacingAdd = reflowable.getLineSpacingAdd();
            lineSpacingMult = reflowable.getLineSpacingMult();
            textWidth = reflowable.getTextWidth();
            breakStrategy = reflowable.getBreakStrategy();
            letterSpacing = reflowable.getLetterSpacing();
            maxLines = reflowable.getMaxLines();
        }

        ReflowData(Parcel in) {
            text = in.readString();
            textSize = in.readFloat();
            textColor = in.readInt();
            bounds = (Rect) in.readValue(Rect.class.getClassLoader());
            fontResId = in.readInt();
            lineSpacingAdd = in.readFloat();
            lineSpacingMult = in.readFloat();
            textPosition = (Point) in.readValue(Point.class.getClassLoader());
            textHeight = in.readInt();
            textWidth = in.readInt();
            breakStrategy = in.readInt();
            letterSpacing = in.readFloat();
            maxLines = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(text);
            dest.writeFloat(textSize);
            dest.writeInt(textColor);
            dest.writeValue(bounds);
            dest.writeInt(fontResId);
            dest.writeFloat(lineSpacingAdd);
            dest.writeFloat(lineSpacingMult);
            dest.writeValue(textPosition);
            dest.writeInt(textHeight);
            dest.writeInt(textWidth);
            dest.writeInt(breakStrategy);
            dest.writeFloat(letterSpacing);
            dest.writeInt(maxLines);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<ReflowData> CREATOR
                = new Parcelable.Creator<ReflowData>() {
            @Override
            public ReflowData createFromParcel(Parcel in) {
                return new ReflowData(in);
            }

            @Override
            public ReflowData[] newArray(int size) {
                return new ReflowData[size];
            }
        };

    }

    /**
     * Models the location of a run of text in both start and end states.
     */
    private static class Run {

        final Rect start;
        final boolean startVisible;
        final Rect end;
        final boolean endVisible;

        Run(Rect start, boolean startVisible, Rect end, boolean endVisible) {
            this.start = start;
            this.startVisible = startVisible;
            this.end = end;
            this.endVisible = endVisible;
        }
    }

    /**
     * A drawable which shows (a portion of) one of two given bitmaps, switching between them once
     * a progress property passes a threshold.
     * <p>
     * This is helpful when animating text size change as small text scaled up is blurry but larger
     * text scaled down has different kerning. Instead we use images of both states and switch
     * during the transition. We use images as animating text size thrashes the font cache.
     */
    private static class SwitchDrawable extends Drawable {

        static final Property<SwitchDrawable, PointF> TOP_LEFT =
                new Property<SwitchDrawable, PointF>(PointF.class, "topLeft") {
                    @Override
                    public void set(SwitchDrawable drawable, PointF topLeft) {
                        drawable.setTopLeft(topLeft);
                    }

                    @Override
                    public PointF get(SwitchDrawable drawable) {
                        return drawable.getTopLeft();
                    }
                };

        static final Property<SwitchDrawable, Integer> WIDTH =
                new Property<SwitchDrawable, Integer>(Integer.class, "width") {
                    @Override
                    public void set(SwitchDrawable drawable, Integer width) {
                        drawable.setWidth(width);
                    }

                    @Override
                    public Integer get(SwitchDrawable drawable) {
                        return drawable.getWidth();
                    }
                };

        static final Property<SwitchDrawable, Integer> HEIGHT =
                new Property<SwitchDrawable, Integer>(Integer.class, "height") {
                    @Override
                    public void set(SwitchDrawable drawable, Integer height) {
                        drawable.setHeight(height);
                    }

                    @Override
                    public Integer get(SwitchDrawable drawable) {
                        return drawable.getHeight();
                    }
                };

        static final Property<SwitchDrawable, Integer> ALPHA =
                new Property<SwitchDrawable, Integer>(Integer.class, "alpha") {
                    @Override
                    public void set(SwitchDrawable drawable, Integer alpha) {
                        drawable.setAlpha(alpha);
                    }

                    @Override
                    public Integer get(SwitchDrawable drawable) {
                        return drawable.getAlpha();
                    }
                };

        static final Property<SwitchDrawable, Float> PROGRESS =
                new Property<SwitchDrawable, Float>(Float.class, "progress") {
                    @Override
                    public void set(SwitchDrawable drawable, Float progress) {
                        drawable.setProgress(progress);
                    }

                    @Override
                    public Float get(SwitchDrawable drawable) {
                        return 0f;
                    }
                };

        private final Paint paint;
        private final float switchThreshold;
        private Bitmap currentBitmap;
        private final Bitmap endBitmap;
        private Rect currentBitmapSrcBounds;
        private final Rect endBitmapSrcBounds;
        private boolean hasSwitched = false;
        private PointF topLeft;
        private int width, height;

        SwitchDrawable(
                @NonNull Bitmap startBitmap,
                @NonNull Rect startBitmapSrcBounds,
                float startFontSize,
                @NonNull Bitmap endBitmap,
                @NonNull Rect endBitmapSrcBounds,
                float endFontSize) {
            currentBitmap = startBitmap;
            currentBitmapSrcBounds = startBitmapSrcBounds;
            this.endBitmap = endBitmap;
            this.endBitmapSrcBounds = endBitmapSrcBounds;
            switchThreshold = startFontSize / (startFontSize + endFontSize);
            paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawBitmap(currentBitmap, currentBitmapSrcBounds, getBounds(), paint);
        }

        @Override
        public int getAlpha() {
            return paint.getAlpha();
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public ColorFilter getColorFilter() {
            return paint.getColorFilter();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        void setProgress(float progress) {
            if (!hasSwitched && progress >= switchThreshold) {
                currentBitmap = endBitmap;
                currentBitmapSrcBounds = endBitmapSrcBounds;
                hasSwitched = true;
            }
        }

        PointF getTopLeft() {
            return topLeft;
        }

        void setTopLeft(PointF topLeft) {
            this.topLeft = topLeft;
            updateBounds();
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
            updateBounds();
        }

        int getHeight() {
            return height;
        }

        void setHeight(int height) {
            this.height = height;
            updateBounds();
        }

        private void updateBounds() {
            int left = Math.round(topLeft.x);
            int top = Math.round(topLeft.y);
            setBounds(left, top, left + width, top + height);
        }
    }

    /**
     * Interface describing a view which supports re-flowing i.e. it exposes enough information to
     * construct a {@link ReflowData} object;
     */
    public interface Reflowable<T extends View> {

        T getView();
        String getText();
        Point getTextPosition();
        int getTextWidth();
        int getTextHeight();
        float getTextSize();
        @ColorInt int getTextColor();
        float getLineSpacingAdd();
        float getLineSpacingMult();
        int getBreakStrategy();
        float getLetterSpacing();
        @FontRes int getFontResId();
        int getMaxLines();
    }

    /**
     * Wraps a {@link TextView} and implements {@link Reflowable}.
     */
    public static class ReflowableTextView implements Reflowable<TextView> {

        private final BaselineGridTextView textView;

        public ReflowableTextView(BaselineGridTextView textView) {
            this.textView = textView;
        }

        @Override
        public TextView getView() {
            return textView;
        }

        @Override
        public String getText() {
            return textView.getText().toString();
        }

        @Override
        public Point getTextPosition() {
            return new Point(textView.getCompoundPaddingLeft(), textView.getCompoundPaddingTop());
        }

        @Override
        public int getTextWidth() {
            return textView.getWidth()
                    - textView.getCompoundPaddingLeft() - textView.getCompoundPaddingRight();
        }

        @Override
        public int getTextHeight() {
            if (textView.getMaxLines() != -1) {
                return (textView.getMaxLines() * textView.getLineHeight()) + 1;
            } else {
                return textView.getHeight() - textView.getCompoundPaddingTop()
                        - textView.getCompoundPaddingBottom();
            }
        }

        @Override
        public float getLineSpacingAdd() {
            return textView.getLineSpacingExtra();
        }

        @Override
        public float getLineSpacingMult() {
            return textView.getLineSpacingMultiplier();
        }

        @Override
        public int getBreakStrategy() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return textView.getBreakStrategy();
            }
            return -1;
        }

        @Override
        public float getLetterSpacing() {
            return textView.getLetterSpacing();
        }

        @Override
        public int getFontResId() {
            return textView.getFontResId();
        }

        @Override
        public float getTextSize() {
            return textView.getTextSize();
        }

        @Override
        public int getTextColor() {
            return textView.getCurrentTextColor();
        }

        @Override
        public int getMaxLines() {
            return textView.getMaxLines();
        }
    }

}
