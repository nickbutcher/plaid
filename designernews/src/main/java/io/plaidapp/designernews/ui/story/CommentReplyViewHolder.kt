/*
 * Copyright 2018 Google LLC.
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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.designernews.R

/**
 * View holder for a Designer News comment reply.
 * TODO move more CommentReply related actions here
 */
internal class CommentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val commentVotes: TextView = itemView.findViewById(R.id.comment_votes)
    private val replyLabel: TextInputLayout = itemView.findViewById(R.id.comment_reply_label)
    val commentReply: EditText = itemView.findViewById(R.id.comment_reply)
    val postReply: ImageButton = itemView.findViewById(R.id.post_reply)

    fun bindCommentReply(comment: Comment) {
        commentVotes.text = comment.upvotesCount.toString()
        commentVotes.isActivated = comment.upvoted
    }

    fun createCommentReplyFocusAnimator(): Animator {
        val interpolator = getFastOutSlowInInterpolator(itemView.context)

        val commentVotesAnimator = ObjectAnimator.ofPropertyValuesHolder(
            commentVotes,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -commentVotes.width.toFloat()),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f)
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }
        val replyLabelAnimator = ObjectAnimator.ofPropertyValuesHolder(
            replyLabel,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -commentVotes.width.toFloat())
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }

        postReply.visibility = View.VISIBLE
        postReply.alpha = 0f

        val postReplyAnimator = ObjectAnimator.ofPropertyValuesHolder(
            postReply,
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }

        return AnimatorSet().apply {
            playTogether(commentVotesAnimator, replyLabelAnimator, postReplyAnimator)
            doOnStart {
                itemView.setHasTransientState(true)
            }
            doOnEnd {
                itemView.setHasTransientState(false)
            }
        }
    }

    fun createCommentReplyFocusLossAnimator(): Animator {
        val interpolator = getFastOutSlowInInterpolator(itemView.context)

        val commentVotesAnimator = ObjectAnimator.ofPropertyValuesHolder(
            commentVotes,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f)
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }
        val replyLabelAnimator = ObjectAnimator.ofFloat(
            replyLabel,
            View.TRANSLATION_X,
            0f
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }

        val postReplyAnimator = ObjectAnimator.ofFloat(
            postReply,
            View.ALPHA,
            0f
        ).apply {
            duration = 200L
            this.interpolator = interpolator
        }

        return AnimatorSet().apply {
            playTogether(commentVotesAnimator, replyLabelAnimator, postReplyAnimator)
            doOnStart {
                itemView.setHasTransientState(true)
            }
            doOnEnd {
                postReply.visibility = View.INVISIBLE
                itemView.setHasTransientState(true)
            }
        }
    }
}
