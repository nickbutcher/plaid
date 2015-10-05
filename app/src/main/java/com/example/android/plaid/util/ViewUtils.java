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

package com.example.android.plaid.util;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

/**
 * Utility methods for working with Views.
 */
public class ViewUtils {

    public static int getActionBarSize(Activity activity) {
        TypedValue value = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
        return TypedValue.complexToDimensionPixelSize(value.data, activity.getResources()
                .getDisplayMetrics());
    }

    public static RippleDrawable createRipple(@ColorInt int color, @FloatRange(from = 0f, to =
            1f) float alpha) {
        color = ColorUtils.modifyAlpha(color, alpha);
        return new RippleDrawable(ColorStateList.valueOf(color), null, null);
    }

    public static RippleDrawable createMaskedRipple(@ColorInt int color, @FloatRange(from = 0f,
            to = 1f) float alpha) {
        color = ColorUtils.modifyAlpha(color, alpha);
        return new RippleDrawable(ColorStateList.valueOf(color), null, new ColorDrawable
                (0xffffffff));
    }

    public static void setLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static void clearLightStatusBar(@NonNull View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
        }
    }

    public static final Property<View, Integer> BACKGROUND_COLOR = new AnimUtils
            .IntProperty<View>("backgroundColor") {

        @Override
        public void setValue(View view, int value) {
            view.setBackgroundColor(value);
        }

        @Override
        public Integer get(View view) {
            Drawable d = view.getBackground();
            if (d instanceof ColorDrawable) {
                return ((ColorDrawable) d).getColor();
            }
            return Color.TRANSPARENT;
        }
    };

    public static final Property<ImageView, Integer> IMAGE_ALPHA = new AnimUtils
            .IntProperty<ImageView>("imageAlpha") {

        @Override
        public void setValue(ImageView imageView, int value) {
            imageView.setImageAlpha(value);
        }

        @Override
        public Integer get(ImageView imageView) {
            return imageView.getImageAlpha();
        }
    };
}
