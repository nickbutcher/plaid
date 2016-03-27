package io.plaidapp.util.compat;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Local TextView compat functions that don't exist in {@link android.support.v4.widget.TextViewCompat}
 */
public final class LocalTextViewCompat {

    private LocalTextViewCompat() {
        throw new AssertionError("No instances.");
    }

    @ColorInt
    public static int getHighlightColor(@NonNull TextView textView) {
        return getHighlightColor(textView, android.R.color.transparent);
    }

    @ColorInt
    public static int getHighlightColor(@NonNull TextView textView, @ColorRes int defaultColorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return textView.getHighlightColor();
        } else {
            return ContextCompat.getColor(textView.getContext(), defaultColorResId);
        }
    }

    public static void setDrawable(int gravity, @NonNull TextView view, @Nullable Drawable drawable,
            boolean useIntrinsic) {
        if (useIntrinsic) {
            switch (gravity) {
                case GravityCompat.START:
                    view.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    break;
                case Gravity.TOP:
                    view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    break;
                case GravityCompat.END:
                    view.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                    break;
                case Gravity.BOTTOM:
                    view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Gravity int: " + gravity);
            }
        } else {
            switch (gravity) {
                case GravityCompat.START:
                    view.setCompoundDrawables(drawable, null, null, null);
                    break;
                case Gravity.TOP:
                    view.setCompoundDrawables(null, drawable, null, null);
                    break;
                case GravityCompat.END:
                    view.setCompoundDrawables(null, null, drawable, null);
                    break;
                case Gravity.BOTTOM:
                    view.setCompoundDrawables(null, null, null, drawable);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Gravity int: " + gravity);
            }
        }
    }
}
