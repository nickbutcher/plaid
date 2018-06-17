package io.plaidapp.ui.designernews.story

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import io.plaidapp.activities.R
import io.plaidapp.ui.recyclerview.SlideInItemAnimator
import io.plaidapp.ui.widget.AuthorTextView
import io.plaidapp.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.util.AnimUtils.getLinearOutSlowInInterpolator

internal class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val threadDepth: ImageView = itemView.findViewById(R.id.depth)
    val author: AuthorTextView = itemView.findViewById(R.id.comment_author)
    val timeAgo: TextView = itemView.findViewById(R.id.comment_time_ago)
    val comment: TextView = itemView.findViewById(R.id.comment_text)

    fun animate(info: DesignerNewsStory.CommentItemHolderInfo,
                animator: SlideInItemAnimator) {
        val expandedThreadOffset = (-(threadDepth.width
                + (threadDepth.layoutParams as ViewGroup.MarginLayoutParams)
                .marginStart)).toFloat()
        val expandedAuthorCommentOffset = (-(threadDepth.width
                + (threadDepth.layoutParams as ViewGroup.MarginLayoutParams)
                .marginEnd)).toFloat()

        if (info.doExpand) {
            expand(expandedThreadOffset, expandedAuthorCommentOffset, animator)
        } else if (info.doCollapse) {
            collapse(expandedThreadOffset,
                    expandedAuthorCommentOffset, animator)
        }
    }

    private fun expand(
            expandedThreadOffset: Float,
            expandedAuthorCommentOffset: Float,
            animator: SlideInItemAnimator
    ) {
        val moveInterpolator = getFastOutSlowInInterpolator(itemView.context)
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

    private fun collapse(
            expandedThreadOffset: Float,
            expandedAuthorCommentOffset: Float,
            animator: SlideInItemAnimator
    ) {
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