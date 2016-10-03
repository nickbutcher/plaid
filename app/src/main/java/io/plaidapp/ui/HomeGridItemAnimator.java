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

package io.plaidapp.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.plaidapp.ui.recyclerview.SlideInItemAnimator;
import io.plaidapp.ui.transitions.GravityArcMotion;
import io.plaidapp.util.AnimUtils;
import io.plaidapp.util.ViewUtils;

/**
 * A {@link RecyclerView.ItemAnimator} for running animations specific to our home grid.
 */
public class HomeGridItemAnimator extends SlideInItemAnimator {

    // Constant payloads, for use with Adapter#notifyItemChanged
    public static final int ADD_TO_POCKET = 1;
    public static final int STORY_COMMENTS_RETURN = 2;

    // Pending animations
    private FeedAdapter.DesignerNewsStoryHolder pendingAddToPocket;
    private FeedAdapter.DesignerNewsStoryHolder pendingStoryCommentsReturn;

    // Currently running animations
    private Pair<FeedAdapter.DesignerNewsStoryHolder, AnimatorSet> runningAddToPocket;
    private Pair<FeedAdapter.DesignerNewsStoryHolder, AnimatorSet> runningStoryCommentsReturn;

    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new HomeGridItemHolderInfo();
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state,
                                                     RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags,
                                                     List<Object> payloads) {
        ItemHolderInfo info =
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
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                 RecyclerView.ViewHolder newHolder,
                                 ItemHolderInfo preInfo,
                                 ItemHolderInfo postInfo) {
        boolean runPending = super.animateChange(oldHolder, newHolder, preInfo, postInfo);

        if (preInfo instanceof HomeGridItemHolderInfo) {
            HomeGridItemHolderInfo info = (HomeGridItemHolderInfo) preInfo;
            if (info.animateAddToPocket) {
                pendingAddToPocket = (FeedAdapter.DesignerNewsStoryHolder) newHolder;
                runPending = true;
            }
            if (info.returnFromComments) {
                pendingStoryCommentsReturn = (FeedAdapter.DesignerNewsStoryHolder) newHolder;
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

    private void animateAddToPocket(final FeedAdapter.DesignerNewsStoryHolder holder) {
        endAnimation(holder);

        // setup for anim
        ((ViewGroup) holder.pocket.getParent().getParent()).setClipChildren(false);
        final int initialLeft = holder.pocket.getLeft();
        final int initialTop = holder.pocket.getTop();
        final int translatedLeft =
                (holder.itemView.getWidth() - holder.pocket.getWidth()) / 2;
        final int translatedTop =
                initialTop - ((holder.itemView.getHeight() - holder.pocket.getHeight()) / 2);
        final GravityArcMotion arc = new GravityArcMotion();

        // animate the title & pocket icon up, scale the pocket icon up
        Animator titleMoveFadeOut = ObjectAnimator.ofPropertyValuesHolder(holder.title,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y,
                        -(holder.itemView.getHeight() / 5)),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.54f));

        Animator pocketMoveUp = ObjectAnimator.ofFloat(holder.pocket,
                View.TRANSLATION_X, View.TRANSLATION_Y,
                arc.getPath(initialLeft, initialTop, translatedLeft, translatedTop));
        Animator pocketScaleUp = ObjectAnimator.ofPropertyValuesHolder(holder.pocket,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 3f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 3f));
        ObjectAnimator pocketFadeUp = ObjectAnimator.ofInt(holder.pocket,
                ViewUtils.IMAGE_ALPHA, 255);

        AnimatorSet up = new AnimatorSet();
        up.playTogether(titleMoveFadeOut, pocketMoveUp, pocketScaleUp, pocketFadeUp);
        up.setDuration(300L);
        up.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(holder.itemView.getContext()));

        // animate everything back into place
        Animator titleMoveFadeIn = ObjectAnimator.ofPropertyValuesHolder(holder.title,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f));
        Animator pocketMoveDown = ObjectAnimator.ofFloat(holder.pocket,
                View.TRANSLATION_X, View.TRANSLATION_Y,
                arc.getPath(translatedLeft, translatedTop, 0, 0));
        Animator pvhPocketScaleDown = ObjectAnimator.ofPropertyValuesHolder(holder.pocket,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f));
        ObjectAnimator pocketFadeDown = ObjectAnimator.ofInt(holder.pocket,
                ViewUtils.IMAGE_ALPHA, 178);

        AnimatorSet down = new AnimatorSet();
        down.playTogether(titleMoveFadeIn, pocketMoveDown, pvhPocketScaleDown, pocketFadeDown);
        down.setStartDelay(500L);
        down.setDuration(300L);
        down.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(holder.itemView.getContext()));

        AnimatorSet addToPocketAnim = new AnimatorSet();
        addToPocketAnim.playSequentially(up, down);

        addToPocketAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                dispatchChangeStarting(holder, false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((ViewGroup) holder.pocket.getParent().getParent()).setClipChildren(true);
                runningAddToPocket = null;
                dispatchChangeFinished(holder, false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                holder.title.setAlpha(1f);
                holder.title.setTranslationY(0f);
                holder.pocket.setTranslationX(0f);
                holder.pocket.setTranslationY(0f);
                holder.pocket.setScaleX(1f);
                holder.pocket.setScaleY(1f);
                holder.pocket.setImageAlpha(178);
                runningAddToPocket = null;
                dispatchChangeFinished(holder, false);
            }
        });
        runningAddToPocket = Pair.create(holder, addToPocketAnim);
        addToPocketAnim.start();
    }

    private void animateStoryCommentReturn(final FeedAdapter.DesignerNewsStoryHolder holder) {
        endAnimation(holder);

        AnimatorSet commentsReturnAnim = new AnimatorSet();
        commentsReturnAnim.playTogether(
                ObjectAnimator.ofFloat(holder.pocket, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(holder.comments, View.ALPHA, 0f, 1f));
        commentsReturnAnim.setDuration(120L);
        commentsReturnAnim.setInterpolator(
                AnimUtils.getLinearOutSlowInInterpolator(holder.itemView.getContext()));
        commentsReturnAnim.addListener(new AnimatorListenerAdapter() {
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
                holder.pocket.setAlpha(1f);
                holder.comments.setAlpha(1f);
                runningStoryCommentsReturn = null;
                dispatchChangeFinished(holder, false);
            }
        });
        runningStoryCommentsReturn = Pair.create(holder, commentsReturnAnim);
        commentsReturnAnim.start();
    }

    private class HomeGridItemHolderInfo extends ItemHolderInfo {
        boolean animateAddToPocket;
        boolean returnFromComments;
    }

}
