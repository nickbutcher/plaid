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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.support.design.widget.TextInputEditText;
import android.text.TextPaint;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Property;
import android.view.animation.Interpolator;

import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ColorUtils;
import io.plaidapp.util.ViewUtils;

/**
 * A password entry widget which animates switching between masked and visible text.
 */
public class PasswordEntry extends TextInputEditText {

    private boolean passwordMasked = false;
    private MaskMorphDrawable maskDrawable;

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
            passwordVisibilityToggled(isMasked);
        }
    }

    private void passwordVisibilityToggled(boolean isMasked) {
        if (maskDrawable == null) {
            // lazily create the drawable that morphs the dots
            if (!isLaidOut() || getText().length() < 1) return;
            maskDrawable = new MaskMorphDrawable(getContext(), getPaint(), getBaseline(),
                    getLayout().getPrimaryHorizontal(1), getInsetStart());
            maskDrawable.setBounds(getPaddingLeft(), getPaddingTop(), 0,
                    getHeight() - getPaddingTop() - getPaddingBottom());
            getOverlay().add(maskDrawable);
        }
        maskDrawable.setDotCount(getText().length());

        // also animate the text color to cross fade
        final ColorStateList textColors = getTextColors();
        int currentColor = getCurrentTextColor();
        int fadedOut = ColorUtils.modifyAlpha(currentColor, 0);
        Animator morph;
        if (isMasked) {
            // text has already changed to dots so can't cross fade, just hide it
            morph = maskDrawable.createShowMaskAnimator();
            setTextColor(fadedOut);
        } else {
            Animator mask = maskDrawable.createHideMaskAnimator();
            setTextColor(fadedOut); // set immediately because of start delay
            Animator fadeText =
                    ObjectAnimator.ofArgb(this, ViewUtils.TEXT_COLOR, fadedOut, currentColor);
            fadeText.setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(getContext()));
            fadeText.setStartDelay(120L);
            fadeText.setDuration(180L);
            morph = new AnimatorSet();
            ((AnimatorSet) morph).playTogether(mask, fadeText);
        }
        morph.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // restore the proper text color & hide the drawable
                setTextColor(textColors);
                maskDrawable.setAlpha(0);
            }
        });
        morph.start();
    }

    private int getInsetStart() {
        int insetStart = 0;
        if (getBackground() instanceof InsetDrawable) {
            InsetDrawable back = (InsetDrawable) getBackground();
            Rect padding = new Rect();
            back.getPadding(padding);
            insetStart = padding.left;
        }
        return insetStart;
    }

    /**
     * A drawable for animating the switch between a masked and visible password field.
     */
    static class MaskMorphDrawable extends Drawable {

        private static final char[] PASSWORD_MASK = { '•' };

        private final Paint paint;
        private final float charWidth;
        private final float maskCharRadius;
        private final float unmaskedRadius;
        private final float maskCenterY;
        private final float insetStart;
        private final float maxOffsetY;
        private final Interpolator linearOutSlowInInterpolator;
        private final Interpolator fastOutLinearInInterpolator;

        private int dotCount;
        private float dotRadius;
        private float dotOffsetY;

        MaskMorphDrawable(Context context, TextPaint textPaint,
                          int baseline, float charWidth, int insetStart) {
            this.insetStart = insetStart;
            this.charWidth = charWidth;
            unmaskedRadius = charWidth / 2f;
            Rect rect = new Rect();
            textPaint.getTextBounds(PASSWORD_MASK, 0, 1, rect);
            maskCharRadius = rect.height() / 2f;
            maskCenterY = (baseline + rect.top + baseline + rect.bottom) / 2f;
            maxOffsetY = charWidth / 5f;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(textPaint.getColor());
            fastOutLinearInInterpolator = AnimUtils.getFastOutLinearInInterpolator(context);
            linearOutSlowInInterpolator = AnimUtils.getLinearOutSlowInInterpolator(context);
        }

        float getDotRadius() {
            return dotRadius;
        }

        void setDotRadius(float dotRadius) {
            if (this.dotRadius != dotRadius) {
                this.dotRadius = dotRadius;
                invalidateSelf();
            }
        }

        public float getDotOffsetY() {
            return dotOffsetY;
        }

        public void setDotOffsetY(float dotOffsetY) {
            if (this.dotOffsetY != dotOffsetY) {
                this.dotOffsetY = dotOffsetY;
                invalidateSelf();
            }
        }

        Animator createShowMaskAnimator() {
            return animateMask(unmaskedRadius, maskCharRadius, 0, 255, maxOffsetY, 0f, 120L);
        }

        Animator createHideMaskAnimator() {
            return animateMask(maskCharRadius, unmaskedRadius, 192, 0, 0f, maxOffsetY, 200L);
        }

        void setDotCount(int dotCount) {
            if (dotCount != this.dotCount) {
                this.dotCount = dotCount;
                Rect bounds = getBounds();
                setBounds(
                        bounds.left,
                        bounds.top,
                        bounds.left + (int) Math.ceil(dotCount * charWidth),
                        bounds.bottom);
                invalidateSelf();
            }
        }

        @Override
        public void draw(Canvas canvas) {
            float x = insetStart + (charWidth / 2f);
            float y = maskCenterY + dotOffsetY;
            for (int i = 0; i < dotCount; i++) {
                canvas.drawCircle(x, y, dotRadius, paint);
                x += charWidth;
            }
        }

        @Override
        public void setAlpha(int alpha) {
            if (alpha != getAlpha()) {
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

        private Animator animateMask(
                float startRadius, float endRadius,
                int startAlpha, int endAlpha,
                float startOffsetY, float endOffsetY,
                long duration) {
            PropertyValuesHolder radius =
                    PropertyValuesHolder.ofFloat(RADIUS, startRadius, endRadius);
            PropertyValuesHolder alpha =
                    PropertyValuesHolder.ofInt(ViewUtils.DRAWABLE_ALPHA, startAlpha, endAlpha);
            // animate the y offset slightly as the vertical center of the password mask dot
            // is higher than the text x-height so this smooths the change
            PropertyValuesHolder offset =
                    PropertyValuesHolder.ofFloat(OFFSET_Y, startOffsetY, endOffsetY);
            ObjectAnimator anim =
                    ObjectAnimator.ofPropertyValuesHolder(this, radius, alpha, offset);
            anim.setDuration(duration);
            if (startRadius > endRadius) {
                anim.setInterpolator(linearOutSlowInInterpolator);
            } else {
                anim.setInterpolator(fastOutLinearInInterpolator);
            }
            return anim;
        }

        static final Property<MaskMorphDrawable, Float> RADIUS
                = new AnimUtils.FloatProperty<MaskMorphDrawable>("dotRadius") {

            @Override
            public void setValue(MaskMorphDrawable drawable, float radius) {
                drawable.setDotRadius(radius);
            }

            @Override
            public Float get(MaskMorphDrawable drawable) {
                return drawable.getDotRadius();
            }
        };

        static final Property<MaskMorphDrawable, Float> OFFSET_Y
                = new AnimUtils.FloatProperty<MaskMorphDrawable>("dotOffsetY") {

            @Override
            public void setValue(MaskMorphDrawable drawable, float offset) {
                drawable.setDotOffsetY(offset);
            }

            @Override
            public Float get(MaskMorphDrawable drawable) {
                return drawable.getDotOffsetY();
            }
        };

    }
}
