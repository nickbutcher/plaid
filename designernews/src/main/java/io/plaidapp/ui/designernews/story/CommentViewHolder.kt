/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.ui.designernews.story

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.plaidapp.base.ui.recyclerview.SlideInItemAnimator
import io.plaidapp.base.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.base.util.AnimUtils.getLinearOutSlowInInterpolator
import io.plaidapp.designernews.R
import io.plaidapp.ui.widget.AuthorTextView

internal class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val threadDepth: ImageView = itemView.findViewById(R.id.depth)
    val author: AuthorTextView = itemView.findViewById(R.id.comment_author)
    val timeAgo: TextView = itemView.findViewById(R.id.comment_time_ago)
    val comment: TextView = itemView.findViewById(R.id.comment_text)

    private fun getExpandedAuthorCommentOffset(): Float {
        return (-(threadDepth.width +
                (threadDepth.layoutParams as ViewGroup.MarginLayoutParams)
                        .marginEnd)).toFloat()
    }

    private fun getExpandedThreadOffset(): Float {
        return (-(threadDepth.width +
                (threadDepth.layoutParams as ViewGroup.MarginLayoutParams)
                        .marginStart)).toFloat()
    }

    fun expand(animator: SlideInItemAnimator) {
        val expandedThreadOffset = getExpandedThreadOffset()
        val expandedAuthorCommentOffset = getExpandedAuthorCommentOffset()
        val moveInterpolator = getFastOutSlowInInterpolator(itemView.context)

        // TODO: Nick - extract the animator here

        threadDepth.translationX = 0f
        threadDepth.animate().apply {
            translationX(expandedThreadOffset)
            duration = 160L
            interpolator = moveInterpolator
        }
        author.translationX = 0f
        author.animate().apply {
            translationX(expandedAuthorCommentOffset)
            duration = 320L
            interpolator = moveInterpolator
        }
        comment.translationX = 0f
        comment.animate().apply {
            translationX(expandedAuthorCommentOffset)
            duration = 320L
            interpolator = moveInterpolator
            setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    animator.dispatchChangeStarting(this@CommentViewHolder, false)
                    itemView.setHasTransientState(true)
                }

                override fun onAnimationEnd(animation: Animator) {
                    itemView.setHasTransientState(false)
                    animator.dispatchChangeFinished(this@CommentViewHolder, false)
                }
            })
        }
    }

    fun collapse(
            animator: SlideInItemAnimator
    ) {
        val expandedThreadOffset = getExpandedThreadOffset()
        val expandedAuthorCommentOffset = getExpandedAuthorCommentOffset()

        val enterInterpolator = getLinearOutSlowInInterpolator(itemView.context)
        val moveInterpolator = getFastOutSlowInInterpolator(itemView.context)

        // return the thread depth indicator into place
        threadDepth.translationX = expandedThreadOffset
        threadDepth.animate().apply {
            translationX(0f)
            duration = 200L
            interpolator = enterInterpolator
            setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    animator.dispatchChangeStarting(this@CommentViewHolder, false)
                    itemView.setHasTransientState(true)
                }

                override fun onAnimationEnd(animation: Animator) {
                    itemView.setHasTransientState(false)
                    animator.dispatchChangeFinished(this@CommentViewHolder, false)
                }
            })
        }

        // return the text into place
        author.translationX = expandedAuthorCommentOffset
        author.animate().apply {
            translationX(0f)
            duration = 200L
            interpolator = moveInterpolator
        }
        comment.translationX = expandedAuthorCommentOffset
        comment.animate().apply {
            translationX(0f)
            duration = 200L
            interpolator = moveInterpolator
        }
    }
}
