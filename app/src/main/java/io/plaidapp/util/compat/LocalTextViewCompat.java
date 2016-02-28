package io.plaidapp.util.compat;

import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
}
