package io.plaidapp.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * When there is a layout change,use transition way
 */
public class TransitionFrameLayout extends FrameLayout {
    public TransitionFrameLayout(@NonNull Context context) {
        super(context);
    }

    public TransitionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            TransitionManager.beginDelayedTransition(this, new AutoTransition().setDuration(125));
        }
        super.onLayout(changed, left, top, right, bottom);
    }
}
