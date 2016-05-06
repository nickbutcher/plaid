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

package io.plaidapp.ui.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.plaidapp.util.AnimUtils;

/**
 * A RecyclerView Item animator that slides new items in upward
 */
public class SlideInItemAnimator extends DefaultItemAnimator {

    private final List<RecyclerView.ViewHolder> pendingAdds = new ArrayList<>();

    public SlideInItemAnimator() {
        setAddDuration(160L);
    }

    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(holder.itemView.getHeight() / 3);
        pendingAdds.add(holder);
        return true;
    }

    @Override
    public void runPendingAnimations() {
        super.runPendingAnimations();
        if (!pendingAdds.isEmpty()) {
            for (final RecyclerView.ViewHolder holder : pendingAdds) {
                holder.itemView.animate()
                        .alpha(1f)
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
                                pendingAdds.remove(holder);
                                if (!isRunning()) dispatchAnimationsFinished();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                holder.itemView.setAlpha(1f);
                                holder.itemView.setTranslationY(0f);
                            }
                        })
                        .setInterpolator(
                                AnimUtils.getLinearOutSlowInInterpolator(holder.itemView.getContext()));
            }
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning() || !pendingAdds.isEmpty();
    }

}
