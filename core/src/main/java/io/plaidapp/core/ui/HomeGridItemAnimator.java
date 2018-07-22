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

package io.plaidapp.core.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemAnimator;
import android.util.Pair;

import java.util.List;

import io.plaidapp.core.designernews.ui.stories.StoryViewHolder;
import io.plaidapp.core.ui.recyclerview.SlideInItemAnimator;


/**
 * A {@link ItemAnimator} for running animations specific to our home grid.
 */
public class HomeGridItemAnimator extends SlideInItemAnimator {

    // Constant payloads, for use with Adapter#notifyItemChanged
    public static final int ADD_TO_POCKET = 1;
    public static final int STORY_COMMENTS_RETURN = 2;

    // Pending animations
    private StoryViewHolder pendingAddToPocket;
    private StoryViewHolder pendingStoryCommentsReturn;

    // Currently running animations
    private Pair<StoryViewHolder, Animator> runningAddToPocket;
    private Pair<StoryViewHolder, Animator> runningStoryCommentsReturn;

    @Override
    public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public ItemAnimator.ItemHolderInfo obtainHolderInfo() {
        return new HomeGridItemHolderInfo();
    }

    @NonNull
    @Override
    public ItemAnimator.ItemHolderInfo recordPreLayoutInformation(
            @NonNull RecyclerView.State state,
            @NonNull RecyclerView.ViewHolder viewHolder,
            int changeFlags,
            @NonNull List<Object> payloads) {
        ItemAnimator.ItemHolderInfo info =
                super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
        if (info instanceof HomeGridItemHolderInfo) {
            HomeGridItemHolderInfo dnInfo = (HomeGridItemHolderInfo) info;
            dnInfo.animateAddToPocket = payloads.contains(ADD_TO_POCKET);
            dnInfo.returnFromComments = payloads.contains(STORY_COMMENTS_RETURN);
            return dnInfo;
        }
        return info;
    }

    @Override
    public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
            @NonNull RecyclerView.ViewHolder newHolder,
            @NonNull ItemAnimator.ItemHolderInfo preInfo,
            @NonNull ItemAnimator.ItemHolderInfo postInfo) {
        boolean runPending = super.animateChange(oldHolder, newHolder, preInfo, postInfo);

        if (preInfo instanceof HomeGridItemHolderInfo) {
            HomeGridItemHolderInfo info = (HomeGridItemHolderInfo) preInfo;
            if (info.animateAddToPocket) {
                pendingAddToPocket = (StoryViewHolder) newHolder;
                runPending = true;
            }
            if (info.returnFromComments) {
                pendingStoryCommentsReturn = (StoryViewHolder) newHolder;
                runPending = true;
            }
        }
        return runPending;
    }

    @Override
    public void runPendingAnimations() {
        super.runPendingAnimations();
        if (pendingAddToPocket != null) {
            animateAddToPocket(pendingAddToPocket);
            pendingAddToPocket = null;
        }
        if (pendingStoryCommentsReturn != null) {
            animateStoryCommentReturn(pendingStoryCommentsReturn);
            pendingStoryCommentsReturn = null;
        }
    }

    @Override
    public void endAnimation(RecyclerView.ViewHolder holder) {
        super.endAnimation(holder);
        if (holder == pendingAddToPocket) {
            dispatchChangeFinished(pendingAddToPocket, false);
            pendingAddToPocket = null;
        }
        if (holder == pendingStoryCommentsReturn) {
            dispatchChangeFinished(pendingStoryCommentsReturn, false);
            pendingStoryCommentsReturn = null;
        }
        if (runningAddToPocket != null && runningAddToPocket.first == holder) {
            runningAddToPocket.second.cancel();
        }
        if (runningStoryCommentsReturn != null && runningStoryCommentsReturn.first == holder) {
            runningStoryCommentsReturn.second.cancel();
        }
    }

    @Override
    public void endAnimations() {
        super.endAnimations();
        if (pendingAddToPocket != null) {
            dispatchChangeFinished(pendingAddToPocket, false);
            pendingAddToPocket = null;
        }
        if (pendingStoryCommentsReturn != null) {
            dispatchChangeFinished(pendingStoryCommentsReturn, false);
            pendingStoryCommentsReturn = null;
        }
        if (runningAddToPocket != null) {
            runningAddToPocket.second.cancel();
        }
        if (runningStoryCommentsReturn != null) {
            runningStoryCommentsReturn.second.cancel();
        }
    }

    @Override
    public boolean isRunning() {
        return super.isRunning()
                || (runningAddToPocket != null && runningAddToPocket.second.isRunning())
                || (runningStoryCommentsReturn != null
                && runningStoryCommentsReturn.second.isRunning());
    }

    private void animateAddToPocket(final StoryViewHolder holder) {
        endAnimation(holder);

        Animator addToPocketAnim = holder.createAddToPocketAnimator();
        addToPocketAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchChangeStarting(holder, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                runningAddToPocket = null;
                dispatchChangeFinished(holder, false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                runningAddToPocket = null;
                dispatchChangeFinished(holder, false);
            }
        });
        runningAddToPocket = Pair.create(holder, addToPocketAnim);
        addToPocketAnim.start();
    }

    private void animateStoryCommentReturn(final StoryViewHolder holder) {
        endAnimation(holder);

        Animator animator = holder.createStoryCommentReturnAnimator();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchChangeStarting(holder, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                runningStoryCommentsReturn = null;
                dispatchChangeFinished(holder, false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                runningStoryCommentsReturn = null;
                dispatchChangeFinished(holder, false);
            }
        });
        runningStoryCommentsReturn = Pair.create(holder, animator);
        animator.start();
    }

    private class HomeGridItemHolderInfo extends ItemAnimator.ItemHolderInfo {
        boolean animateAddToPocket;
        boolean returnFromComments;
    }

}
