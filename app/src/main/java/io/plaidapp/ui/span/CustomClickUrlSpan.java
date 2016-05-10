package io.plaidapp.ui.span;

import android.content.res.ColorStateList;
import android.view.View;

import in.uncod.android.bypass.style.TouchableUrlSpan;

/**
 * A Span that allows different click implementations.
 */
public class CustomClickUrlSpan extends TouchableUrlSpan {
    private OnClickListener mOnClickListener;

    public CustomClickUrlSpan(String url, ColorStateList textColor, int pressedBackgroundColor) {
        super(url, textColor, pressedBackgroundColor);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View widget) {
        if (mOnClickListener == null) {
            super.onClick(widget);
        } else {
            mOnClickListener.onClick(widget, getURL());
        }
    }

    /**
     * Interface definition for a callback to be invoked when a view is clicked.
     */
    public interface OnClickListener {
        void onClick(View view, String url);
    }
}
