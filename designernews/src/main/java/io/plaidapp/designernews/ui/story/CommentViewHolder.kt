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

package io.plaidapp.designernews.ui.story

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.plaidapp.core.ui.recyclerview.SlideInItemAnimator
import io.plaidapp.core.ui.widget.AuthorTextView
import io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.core.util.AnimUtils.getLinearOutSlowInInterpolator
import io.plaidapp.core.util.HtmlUtils
import io.plaidapp.designernews.R
import io.plaidapp.ui.drawable.ThreadedCommentDrawable

internal class CommentViewHolder(
    itemView: View,
    private val threadWidth: Int,
    private val threadGap: Int
) : RecyclerView.ViewHolder(itemView) {

    private val threadDepth: ImageView = itemView.findViewById(R.id.depth)
    private val author: AuthorTextView = itemView.findViewById(R.id.comment_author)
    private val timeAgo: TextView = itemView.findViewById(R.id.comment_time_ago)
    val comment: TextView = itemView.findViewById(R.id.comment_text)

    init {
        threadDepth.setImageDrawable(ThreadedCommentDrawable(threadWidth, threadGap))
    }

    fun bind(model: CommentUiModel) {
        HtmlUtils.setTextWithNiceLinks(comment, model.body)
        author.text = model.author
        author.isOriginalPoster = model.isOriginalPoster
        timeAgo.text = model.timeSinceCommentCreation
        threadDepth.setImageDrawable(
            ThreadedCommentDrawable(threadWidth, threadGap, model.depth)
        )
    }

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

    fun setExpanded(expanded: Boolean) {
        if (expanded) {
            val layoutParams = (threadDepth.layoutParams as ViewGroup.MarginLayoutParams)
            val threadDepthWidth = threadDepth.drawable.intrinsicWidth
            val leftShift = (-(threadDepthWidth + layoutParams.marginEnd)).toFloat()
            author.translationX = leftShift
            comment.translationX = leftShift
            threadDepth.translationX = (-(threadDepthWidth + layoutParams.marginStart)).toFloat()
        } else {
            threadDepth.translationX = 0f
            author.translationX = 0f
            comment.translationX = 0f
        }
    }
}
