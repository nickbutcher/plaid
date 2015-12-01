package io.plaidapp.util.compat;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

public final class ViewAnimationUtilsCompat {

    private static final Animator NOOP = new Animator() {
        @Override
        public long getStartDelay() {
            return 0;
        }

        @Override
        public void setStartDelay(long startDelay) {

        }

        @Override
        public Animator setDuration(long duration) {
            return null;
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public void setInterpolator(TimeInterpolator value) {

        }

        @Override
        public boolean isRunning() {
            return false;
        }
    };

    private ViewAnimationUtilsCompat() {
        throw new AssertionError("No instances.");
    }

    public static Animator createCircularReveal(View view,
            int centerX, int centerY, float startRadius, float endRadius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ViewAnimationUtils.createCircularReveal(view, centerX, centerY, startRadius, endRadius);
        } else {
            return NOOP;
        }
    }
}
