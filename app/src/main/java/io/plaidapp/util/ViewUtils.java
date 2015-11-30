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

package io.plaidapp.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

/**
 * Utility methods for working with Views.
 */
public class ViewUtils {

    private ViewUtils() { }

    private static int actionBarSize = -1;

    public static int getActionBarSize(Context context) {
        if (actionBarSize < 0) {
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
            actionBarSize = TypedValue.complexToDimensionPixelSize(value.data, context
                    .getResources().getDisplayMetrics());
        }
        return actionBarSize;
    }

    public static RippleDrawable createRipple(@ColorInt int color,
                                              @FloatRange(from = 0f, to = 1f) float alpha,
                                              boolean bounded) {
        color = ColorUtils.modifyAlpha(color, alpha);
        return new RippleDrawable(ColorStateList.valueOf(color), null,
                bounded ? new ColorDrawable(Color.WHITE) : null);
    }

    public static RippleDrawable createRipple(@NonNull Palette palette,
                                              @FloatRange(from = 0f, to = 1f) float darkAlpha,
                                              @FloatRange(from = 0f, to = 1f) float lightAlpha,
                                              @ColorInt int fallbackColor,
                                              boolean bounded) {
        int rippleColor = fallbackColor;
        // try the named swatches in preference order
        if (palette.getVibrantSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getVibrantSwatch().getRgb(), darkAlpha);
        } else if (palette.getLightVibrantSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getLightVibrantSwatch().getRgb(),
                    lightAlpha);
        } else if (palette.getDarkVibrantSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getDarkVibrantSwatch().getRgb(),
                    darkAlpha);
        } else if (palette.getMutedSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getMutedSwatch().getRgb(), darkAlpha);
        } else if (palette.getLightMutedSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getLightMutedSwatch().getRgb(),
                    lightAlpha);
        } else if (palette.getDarkMutedSwatch() != null) {
            rippleColor = ColorUtils.modifyAlpha(palette.getDarkMutedSwatch().getRgb(), darkAlpha);
        }
        return new RippleDrawable(ColorStateList.valueOf(rippleColor), null,
                bounded ? new ColorDrawable(Color.WHITE) : null);
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

    /**
     * Recursive binary search to find the best size for the text.
     *
     * Adapted from https://github.com/grantland/android-autofittextview
     */
    public static float getSingleLineTextSize(String text,
                                              TextPaint paint,
                                              float targetWidth,
                                              float low,
                                              float high,
                                              float precision,
                                              DisplayMetrics metrics) {
        final float mid = (low + high) / 2.0f;

        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, metrics));
        final float maxLineWidth = paint.measureText(text);

        if ((high - low) < precision) {
            return low;
        } else if (maxLineWidth > targetWidth) {
            return getSingleLineTextSize(text, paint, targetWidth, low, mid, precision, metrics);
        } else if (maxLineWidth < targetWidth) {
            return getSingleLineTextSize(text, paint, targetWidth, mid, high, precision, metrics);
        } else {
            return mid;
        }
    }

    public static final Property<View, Integer> BACKGROUND_COLOR
            = new AnimUtils.IntProperty<View>("backgroundColor") {

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

    public static final Property<ImageView, Integer> IMAGE_ALPHA
            = new AnimUtils.IntProperty<ImageView>("imageAlpha") {

        @Override
        public void setValue(ImageView imageView, int value) {
            imageView.setImageAlpha(value);
        }

        @Override
        public Integer get(ImageView imageView) {
            return imageView.getImageAlpha();
        }
    };

    public static final ViewOutlineProvider CIRCULAR_OUTLINE = new ViewOutlineProvider() {
        @Override
        public void getOutline(View view, Outline outline) {
            outline.setOval(view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getWidth() - view.getPaddingRight(),
                    view.getHeight() - view.getPaddingBottom());
        }
    };

    /**
     * Determines if two views intersect in the window.
     */
    public static boolean viewsIntersect(View view1, View view2) {
        final int[] view1Loc = new int[2];
        view1.getLocationOnScreen(view1Loc);
        final Rect view1Rect = new Rect(view1Loc[0],
                view1Loc[1],
                view1Loc[0] + view1.getWidth(),
                view1Loc[1] + view1.getHeight());
        int[] view2Loc = new int[2];
        view2.getLocationOnScreen(view2Loc);
        final Rect view2Rect = new Rect(view2Loc[0],
                view2Loc[1],
                view2Loc[0] + view2.getWidth(),
                view2Loc[1] + view2.getHeight());
        return view1Rect.intersect(view2Rect);
    }

}
