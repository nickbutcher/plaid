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
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
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

import io.plaidapp.R;
import io.plaidapp.util.AnimUtils;

import static io.plaidapp.util.AnimUtils.lerp;

/**
 * A password entry widget which animates switching between masked and visible text.
 */
public class PasswordEntry extends TextInputEditText {

    static final char[] PASSWORD_MASK = { '•' };

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
            passwordVisibilityToggled(isMasked, text);
        }
    }

    private void passwordVisibilityToggled(boolean isMasked, CharSequence password) {
        if (maskDrawable == null) {
            // lazily create the drawable that morphs the dots
            if (!isLaidOut() || getText().length() < 1) return;
            maskDrawable = new MaskMorphDrawable(getContext(), getPaint(), getBaseline(),
                    getLayout().getPrimaryHorizontal(1), getTextLeft());
            maskDrawable.setBounds(getPaddingLeft(), getPaddingTop(), 0,
                    getHeight() - getPaddingBottom());
            getOverlay().add(maskDrawable);
        }

        // hide the text during the animation
        final ColorStateList textColors = getTextColors();
        setTextColor(Color.TRANSPARENT);
        Animator morph =  isMasked ?
                maskDrawable.createShowMaskAnimator(password)
                : maskDrawable.createHideMaskAnimator(password);
        morph.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setTextColor(textColors); // restore the proper text color
            }
        });
        morph.start();
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

        private final TextPaint paint;
        private final float charWidth;
        private final float maskDiameter;
        private final float maskCenterY;
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
            Rect maskBounds = new Rect();
            paint.getTextBounds(PASSWORD_MASK, 0, 1, maskBounds);
            maskDiameter = maskBounds.height();
            maskCenterY = (maskBounds.top + maskBounds.bottom) / 2f;
            showPasswordDuration =
                    context.getResources().getInteger(R.integer.show_password_duration);
            hidePasswordDuration =
                    context.getResources().getInteger(R.integer.hide_password_duration);
            fastOutSlowIn = AnimUtils.getFastOutSlowInInterpolator(context);
        }

        public float getMorphProgress() {
            return morphProgress;
        }

        public void setMorphProgress(float morphProgress) {
            if (this.morphProgress != morphProgress) {
                this.morphProgress = morphProgress;
                invalidateSelf();
            }
        }

        Animator createShowMaskAnimator(CharSequence password) {
            return morphPassword(password, 0f, 1f, hidePasswordDuration);
        }

        Animator createHideMaskAnimator(CharSequence password) {
            return morphPassword(password, 1f, 0f, showPasswordDuration);
        }

        @Override
        public void draw(Canvas canvas) {
            if (characters != null && morphProgress != NO_PROGRESS) {
                final int saveCount = canvas.save();
                canvas.translate(insetStart, baseline);
                for (int i = 0; i < characters.length; i++) {
                    characters[i].draw(canvas, paint, password, i, charWidth, morphProgress);
                }
                canvas.restoreToCount(saveCount);
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

        private Animator morphPassword(
                CharSequence pw, float fromProgress, float toProgress, long duration) {
            password = pw;
            updateBounds();
            characters = new PasswordCharacter[pw.length()];
            String passStr = pw.toString();
            for (int i = 0; i < pw.length(); i++) {
                characters[i] = new PasswordCharacter(passStr, i, paint, maskDiameter, maskCenterY);
            }

            Animator anim = ObjectAnimator.ofFloat(this, MORPH, fromProgress, toProgress);
            anim.setDuration(duration);
            anim.setInterpolator(fastOutSlowIn);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    characters = null;
                    morphProgress = NO_PROGRESS;
                    password = null;
                    updateBounds();
                }
            });
            return anim;
        }

        private void updateBounds() {
            Rect oldBounds = getBounds();
            if (password != null) {
                setBounds(oldBounds.left, oldBounds.top,
                        oldBounds.left + (int) Math.ceil(password.length() * charWidth),
                        oldBounds.bottom);
            } else {
                setBounds(oldBounds.left, oldBounds.top, oldBounds.left, oldBounds.bottom);
            }
        }

        static final Property<MaskMorphDrawable, Float> MORPH
                = new AnimUtils.FloatProperty<MaskMorphDrawable>("morphProgress") {

            @Override
            public void setValue(MaskMorphDrawable drawable, float progress) {
                drawable.setMorphProgress(progress);
            }

            @Override
            public Float get(MaskMorphDrawable drawable) {
                return drawable.getMorphProgress();
            }
        };

    }

    /**
     * Models a character in a password, holding info about it's drawing bounds and how it should
     * move/scale to morph to/from the password mask.
     */
    static class PasswordCharacter {

        private final Rect bounds = new Rect();
        private final float textToMaskScale;
        private final float maskToTextScale;
        private final float textOffsetY;

        PasswordCharacter(String password, int index, TextPaint paint,
                          float maskCharDiameter, float maskCenterY) {
            paint.getTextBounds(password, index, index + 1, bounds);
            // scale the mask from the character width, down to it's own width
            maskToTextScale = Math.max(1f, bounds.width() / maskCharDiameter);
            // scale text from it's height down to the mask character height
            textToMaskScale = Math.min(0f, 1f / (bounds.height() / maskCharDiameter));
            // difference between mask & character center
            textOffsetY = maskCenterY - bounds.exactCenterY();
        }


        /**
         * Progress through the morph:  0 = character, 1 = •
         */
        void draw(Canvas canvas, TextPaint paint, CharSequence password,
                  int index, float charWidth, float progress) {
            int alpha = paint.getAlpha();
            float x = charWidth * index;

            // draw the character
            canvas.save();
            float textScale = lerp(1f, textToMaskScale, progress);
            // scale character: shrinks to/grows from the mask's height, remaining centered
            canvas.scale(textScale, textScale, x + bounds.exactCenterX(), bounds.exactCenterY());
            paint.setAlpha((int) lerp(alpha, 0, progress));
            canvas.drawText(password, index, index + 1, x, 0, paint);
            canvas.restore();

            // draw the mask
            canvas.save();
            float maskScale = lerp(maskToTextScale, 1f, progress);
            // scale the mask: down from/up to the character width
            canvas.scale(maskScale, maskScale, x + bounds.exactCenterX(), bounds.exactCenterY());
            paint.setAlpha((int) AnimUtils.lerp(0, alpha, progress));
            // vertically move the mask: character center ↔ mask center
            canvas.drawText(PASSWORD_MASK, 0, 1, x, -lerp(textOffsetY, 0f, progress), paint);
            canvas.restore();
            paint.setAlpha(alpha);
        }

    }
}
