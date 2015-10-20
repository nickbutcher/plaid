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

package io.plaidapp.ui.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.util.Property;

import io.plaidapp.util.AnimUtils;

/**
 * A drawable that can morph size, shape (via it's corner radius) and color.  Specifically this is
 * useful for animating between a FAB and a dialog.
 */
public class MorphDrawable extends Drawable {

    private float cornerRadius;
    public static final Property<MorphDrawable, Float> CORNER_RADIUS = new AnimUtils
            .FloatProperty<MorphDrawable>("cornerRadius") {

        @Override
        public void setValue(MorphDrawable morphDrawable, float value) {
            morphDrawable.setCornerRadius(value);
        }

        @Override
        public Float get(MorphDrawable morphDrawable) {
            return morphDrawable.getCornerRadius();
        }
    };
    private Paint paint;
    public static final Property<MorphDrawable, Integer> COLOR = new AnimUtils
            .IntProperty<MorphDrawable>("color") {

        @Override
        public void setValue(MorphDrawable morphDrawable, int value) {
            morphDrawable.setColor(value);
        }

        @Override
        public Integer get(MorphDrawable morphDrawable) {
            return morphDrawable.getColor();
        }
    };

    public MorphDrawable(@ColorInt int color, float cornerRadius) {
        this.cornerRadius = cornerRadius;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
    }

    public float getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateSelf();
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(getBounds().left, getBounds().top, getBounds().right, getBounds()
                .bottom, cornerRadius, cornerRadius, paint);
    }

    @Override
    public void getOutline(Outline outline) {
        outline.setRoundRect(getBounds(), cornerRadius);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return paint.getAlpha();
    }

}
