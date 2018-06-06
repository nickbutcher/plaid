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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Interpolator;

import java.util.Locale;

import io.plaidapp.R;
import io.plaidapp.util.AnimUtils;

import static io.plaidapp.util.AnimUtils.lerp;

/**
 * A password entry widget which animates switching between masked and visible text.
 */
public class PasswordEntry extends TextInputEditText {

    private static final String TAG = "PasswordEntry";

    private static final boolean DEBUG = false;
    static final char[] PASSWORD_MASK = { '\u2022' }; // PasswordTransformationMethod#DOT

    private boolean passwordMasked;
    private MaskMorphDrawable maskDrawable;
    private DebugDrawable mDebugDrawable;
    private ColorStateList textColor;
    private Animator maskMorph;

    public PasswordEntry(Context context) {
        super(context);
        passwordMasked = getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    public PasswordEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        passwordMasked = getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    public PasswordEntry(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        passwordMasked = getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    /**
     * Want to monitor when password mode is set but #setTransformationMethod is final :( Instead
     * override #setText (which it calls through to) & check the transformation method.
     */
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        boolean isMasked = getTransformationMethod() instanceof PasswordTransformationMethod;
        if (isMasked != passwordMasked) {
            passwordMasked = isMasked;
            passwordVisibilityToggled(isMasked, text);
        }
    }

    @Override
    public void setTextColor(ColorStateList colors) {
        super.setTextColor(colors);
        textColor = colors;
    }

    private void passwordVisibilityToggled(boolean isMasked, CharSequence password) {
        if (maskDrawable == null) {
            // lazily create the drawable that morphs the dots
            if (!isLaidOut() || getText().length() < 1) return;
            maskDrawable = new MaskMorphDrawable(getContext(), getPaint(), getBaseline(),
                    getLayout().getPrimaryHorizontal(1), getTextLeft());
            maskDrawable.setBounds(getPaddingLeft(), getPaddingTop(), getPaddingLeft(),
                    getHeight() - getPaddingBottom());
            getOverlay().add(maskDrawable);
        }

        if (DEBUG) {
            if (mDebugDrawable == null) {
                if (!isLaidOut() || getText().length() < 1) return;
                mDebugDrawable = new DebugDrawable(getBaseline(), getTextLeft(), getPaint());
                mDebugDrawable.setBounds(getPaddingLeft(), getPaddingTop(), getPaddingLeft(),
                        getHeight() - getPaddingBottom());
                getOverlay().add(mDebugDrawable);
            }
            mDebugDrawable.setText(getText().toString(), isMasked);
        }

        // hide the text during the animation
        setTextColor(Color.TRANSPARENT);
        if (maskMorph != null) {
            maskMorph.cancel();
        }
        maskMorph = isMasked ?
                maskDrawable.createShowMaskAnimator(password)
                : maskDrawable.createHideMaskAnimator(password);
        maskMorph.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setTextColor(textColor); // restore the proper text color
            }
        });
        maskMorph.start();
    }

    private int getTextLeft() {
        int left = 0;
        if (getBackground() instanceof InsetDrawable) {
            InsetDrawable back = (InsetDrawable) getBackground();
            Rect padding = new Rect();
            back.getPadding(padding);
            left = padding.left;
        }
        return left;
    }

    /**
     * A drawable for animating the switch between a masked and visible password field.
     */
    static class MaskMorphDrawable extends Drawable {

        private static final float NO_PROGRESS = -1f;
        private static final float PROGRESS_CHARACTER = 0f;
        private static final float PROGRESS_MASK = 1f;

        private final TextPaint paint;
        private Paint mMaskPaint;
        private final float charWidth;
        private final float maskDiameter;
        private final float insetStart;
        private final int baseline;
        private final long showPasswordDuration;
        private final long hidePasswordDuration;
        private final Interpolator fastOutSlowIn;

        private CharSequence password;
        private PasswordCharacter[] characters;
        private float morphProgress;

        MaskMorphDrawable(Context context, TextPaint textPaint,
                          int baseline, float charWidth, int insetStart) {
            this.insetStart = insetStart;
            this.baseline = baseline;
            this.charWidth = charWidth;
            paint = new TextPaint(textPaint);
            mMaskPaint = new Paint();
            mMaskPaint.setColor(Color.GRAY);
            maskDiameter = paint.measureText(PASSWORD_MASK, 0, 1);
            showPasswordDuration =
                    context.getResources().getInteger(R.integer.show_password_duration);
            hidePasswordDuration =
                    context.getResources().getInteger(R.integer.hide_password_duration);
            fastOutSlowIn = AnimUtils.getFastOutSlowInInterpolator(context);
        }

        Animator createShowMaskAnimator(CharSequence password) {
            return morphPassword(password, PROGRESS_CHARACTER, PROGRESS_MASK, hidePasswordDuration);
        }

        Animator createHideMaskAnimator(CharSequence password) {
            return morphPassword(password, PROGRESS_MASK, PROGRESS_CHARACTER, showPasswordDuration);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (characters != null && morphProgress != NO_PROGRESS) {
                final int saveCount = canvas.save();
                canvas.translate(insetStart, baseline);
                for (int i = 0; i < characters.length; i++) {
                    characters[i].draw(canvas, paint, password, i, morphProgress);
                }
                canvas.restoreToCount(saveCount);
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (alpha != paint.getAlpha()) {
                paint.setAlpha(alpha);
                invalidateSelf();
            }
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        private Animator morphPassword(
                CharSequence pw, float fromProgress, float toProgress, long duration) {
            password = pw;
            updateBounds();
            characters = new PasswordCharacter[pw.length()];
            int charX = 0;
            for (int i = 0; i < pw.length(); i++) {
                characters[i] = new PasswordCharacter(i, maskDiameter, charX);
                charX += paint.measureText(password.toString(), i, i + 1);
            }

            ValueAnimator anim = ValueAnimator.ofFloat(fromProgress, toProgress);
            anim.addUpdateListener(animation -> {
                morphProgress = (float) animation.getAnimatedValue();
                invalidateSelf();
            });

            anim.setDuration(duration);
            anim.setInterpolator(fastOutSlowIn);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    characters = null;
                    morphProgress = NO_PROGRESS;
                    password = null;
                    updateBounds();
                    invalidateSelf();
                }
            });
            return anim;
        }

        private void updateBounds() {
            Rect oldBounds = getBounds();
            if (password != null) {
                setBounds(
                        oldBounds.left,
                        oldBounds.top,
                        oldBounds.left + (int) Math.ceil(password.length() * charWidth),
                        oldBounds.bottom);
            } else {
                setBounds(oldBounds.left, oldBounds.top, oldBounds.left, oldBounds.bottom);
            }
        }

    }

    /**
     * debug drawable to show the Password bound
     */
    static class DebugDrawable extends Drawable {
        private Rect mCharBound = new Rect();
        private Rect mMaskBound = new Rect();
        private String mShowText;
        private TextPaint mTextPaint;
        private Paint mBoundPaint;
        private int mTextCount;
        private final int mBaseLine;
        private final int mTextLeft;

        private DebugDrawable(
                int baseLine,
                int textLeft,
                TextPaint paint) {
            mBaseLine = baseLine;
            mTextLeft = textLeft;
            mTextPaint = paint;

            mBoundPaint = new Paint();
            mBoundPaint.setColor(Color.RED);
            mBoundPaint.setStyle(Paint.Style.STROKE);
            mBoundPaint.setStrokeWidth(2f);

            paint.getTextBounds(PASSWORD_MASK, 0, 1, mMaskBound);
        }

        public void setText(String text, boolean isMasked) {
            mShowText = text;
            mTextCount = mShowText.length();
            if (isMasked) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < mTextCount; i++) {
                    builder.append(PASSWORD_MASK[0]);
                }
                mShowText = builder.toString();
            }
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            int count = canvas.save();
            canvas.translate(mTextLeft, mBaseLine);

            int left = 0;
            for (int i = 0; i < mTextCount; i++) {
                float width = mTextPaint.measureText(mShowText, i, i + 1);
                mTextPaint.getTextBounds(mShowText, i, i + 1, mCharBound);
                mCharBound.left += left;
                mCharBound.right += left;
                canvas.drawRect(mCharBound, mBoundPaint);
                left += width;

                Log.v(TAG, String.format(Locale.US,
                        "word of %d width : %f l(%d)r(%d)",
                        i, width, mCharBound.left, mCharBound.right));
            }
            canvas.restoreToCount(count);
        }

        @Override
        public void setAlpha(int alpha) {
            if (mTextPaint.getAlpha() != alpha) {
                mTextPaint.setAlpha(alpha);
                invalidateSelf();
            }
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            mTextPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    /**
     * Models a character in a password, holding info about it's drawing bounds and how it should
     * move/scale to morph to/from the password mask.
     */
    static class PasswordCharacter {

        private final Rect bounds = new Rect();
        private final float textOffsetY;
        private final float maskX;
        private final float charX;

        PasswordCharacter(int index, float maskCharDiameter, float charX) {
            maskX = maskCharDiameter *  index;
            this.charX = charX;
            // scale the mask from the character width, down to it's own width
            textOffsetY = bounds.bottom;
        }


        /**
         * Progress through the morph:  0 = character, 1 = •
         */
        void draw(Canvas canvas, TextPaint paint, CharSequence password,
                  int index, float progress) {
            int alpha = paint.getAlpha();
            float x = lerp(charX, maskX, progress);

            // draw the character
            canvas.save();
            // cross fade between the character/mask
            paint.setAlpha((int) lerp(alpha, 0, progress));
            // vertically move the character center toward/from the mask center
            canvas.drawText(password, index, index + 1, x, textOffsetY, paint);
            canvas.restore();

            // draw the mask
            canvas.save();
            // cross fade between the mask/character
            paint.setAlpha((int) lerp(0, alpha, progress));
            // vertically move the mask center from/toward the character center
            canvas.drawText(PASSWORD_MASK, 0, 1, x, 0, paint);
            canvas.restore();

            // restore the paint to how we found it
            paint.setAlpha(alpha);
        }

    }
}
