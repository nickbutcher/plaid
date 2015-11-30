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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.transition.ArcMotion;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.util.List;

import io.plaidapp.util.ViewUtils;

/**
 * An extension to {@link DefaultItemAnimator} for running animations specific to our home grid.
 */
public class HomeGridItemAnimator extends DefaultItemAnimator {

    public static final int ANIMATE_ADD_POCKET = 7;

    @Override
    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return true;
    }

    @NonNull
    @Override
    public ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state,
                                                     RecyclerView.ViewHolder viewHolder,
                                                     int changeFlags,
                                                     List<Object> payloads) {
        ItemHolderInfo info = super.recordPreLayoutInformation(state, viewHolder, changeFlags,
                payloads);
        if (payloads.contains(ANIMATE_ADD_POCKET)) {
            DesignerNewsItemHolderInfo dnInfo = (DesignerNewsItemHolderInfo) info;
            dnInfo.animateAddToPocket = true;
            return dnInfo;
        }
        return info;
    }

    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                 RecyclerView.ViewHolder newHolder,
                                 ItemHolderInfo preInfo,
                                 ItemHolderInfo postInfo) {
        if (preInfo instanceof DesignerNewsItemHolderInfo
                && ((DesignerNewsItemHolderInfo) preInfo).animateAddToPocket) {
            final FeedAdapter.DesignerNewsStoryHolder holder =
                    (FeedAdapter.DesignerNewsStoryHolder) newHolder;

            // setup for anim
            ((ViewGroup) holder.pocket.getParent().getParent()).setClipChildren(false);
            final int initialLeft = holder.pocket.getLeft();
            final int initialTop = holder.pocket.getTop();
            final int translatedLeft =
                    (holder.itemView.getWidth() - holder.pocket.getWidth()) / 2;
            final int translatedTop =
                    initialTop - ((holder.itemView.getHeight() - holder.pocket.getHeight()) / 2);
            final ArcMotion arc = new ArcMotion();

            // animate the title & pocket icon up, scale the pocket icon up
            Animator titleMoveFadeOut = ObjectAnimator.ofPropertyValuesHolder(holder.title,
                    PropertyValuesHolder.ofFloat(View .TRANSLATION_Y,
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
            up.setDuration(300);
            up.setInterpolator(AnimationUtils.loadInterpolator(holder.itemView.getContext(),
                    android.R.interpolator.fast_out_slow_in));

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
                    ViewUtils.IMAGE_ALPHA, 138);

            AnimatorSet down = new AnimatorSet();
            down.playTogether(titleMoveFadeIn, pocketMoveDown, pvhPocketScaleDown, pocketFadeDown);
            down.setDuration(300);
            down.setInterpolator(AnimationUtils.loadInterpolator(holder.itemView.getContext(),
                    android.R.interpolator.fast_out_slow_in));
            down.setStartDelay(500);

            // play it
            AnimatorSet upDown = new AnimatorSet();
            upDown.playSequentially(up, down);

            // clean up
            upDown.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationStart(Animator animation) {
                    holder.itemView.setHasTransientState(true);
                    dispatchAnimationStarted(holder);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    ((ViewGroup) holder.pocket.getParent().getParent()).setClipChildren(true);
                    holder.itemView.setHasTransientState(false);
                    dispatchAnimationFinished(holder);
                }
            });
            upDown.start();
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
    }

    @Override
    public ItemHolderInfo obtainHolderInfo() {
        return new DesignerNewsItemHolderInfo();
    }

    /* package */ class DesignerNewsItemHolderInfo extends ItemHolderInfo {
        boolean animateAddToPocket;
    }
}
