/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.ui.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.core.util.AnimUtils;

/**
 * A {@link RecyclerView.ItemAnimator} that fades & slides newly added items in from a given
 * direction.
 */
public class SlideInItemAnimator extends DefaultItemAnimator {

    private final List<RecyclerView.ViewHolder> pendingAdds = new ArrayList<>();
    private final int slideFromEdge;

    /**
     * Default to sliding in upward.
     */
    public SlideInItemAnimator() {
        this(Gravity.BOTTOM, -1); // undefined layout dir; bottom isn't relative
    }

    public SlideInItemAnimator(int slideFromEdge, int layoutDirection) {
        this.slideFromEdge = Gravity.getAbsoluteGravity(slideFromEdge, layoutDirection);
        setAddDuration(160L);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0f);
        switch (slideFromEdge) {
            case Gravity.LEFT:
                holder.itemView.setTranslationX(-holder.itemView.getWidth() / 3);
                break;
            case Gravity.TOP:
                holder.itemView.setTranslationY(-holder.itemView.getHeight() / 3);
                break;
            case Gravity.RIGHT:
                holder.itemView.setTranslationX(holder.itemView.getWidth() / 3);
                break;
            default: // Gravity.BOTTOM
                holder.itemView.setTranslationY(holder.itemView.getHeight() / 3);
        }
        pendingAdds.add(holder);
        return true;
    }

    @Override
    public void runPendingAnimations() {
        super.runPendingAnimations();
        if (!pendingAdds.isEmpty()) {
            for (int i = pendingAdds.size() - 1; i >= 0; i--) {
                final RecyclerView.ViewHolder holder = pendingAdds.get(i);
                holder.itemView.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .translationY(0f)
                        .setDuration(getAddDuration())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                dispatchAddStarting(holder);
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animation.getListeners().remove(this);
                                dispatchAddFinished(holder);
                                dispatchFinishedWhenDone();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                clearAnimatedValues(holder.itemView);
                            }
                        })
                        .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator(
                                holder.itemView.getContext()));
                pendingAdds.remove(i);
            }
        }
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder holder) {
        holder.itemView.animate().cancel();
        if (pendingAdds.remove(holder)) {
            dispatchAddFinished(holder);
            clearAnimatedValues(holder.itemView);
        }
        super.endAnimation(holder);
    }

    @Override
    public void endAnimations() {
        for (int i = pendingAdds.size() - 1; i >= 0; i--) {
            final RecyclerView.ViewHolder holder = pendingAdds.get(i);
            clearAnimatedValues(holder.itemView);
            dispatchAddFinished(holder);
            pendingAdds.remove(i);
        }
        super.endAnimations();
    }

    @Override
    public boolean isRunning() {
        return !pendingAdds.isEmpty() || super.isRunning();
    }

    private void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    private void clearAnimatedValues(final View view) {
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
    }

}
