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

import com.google.android.material.textfield.TextInputLayout
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton

import io.plaidapp.designernews.R
import io.plaidapp.core.designernews.domain.model.Comment

/**
 * View holder for a Designer News comment reply.
 * TODO move more CommentReply related actions here
 */
internal class CommentReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val commentVotes: Button = itemView.findViewById(R.id.comment_votes)
    val replyLabel: TextInputLayout = itemView.findViewById(R.id.comment_reply_label)
    val commentReply: EditText = itemView.findViewById(R.id.comment_reply)
    val postReply: ImageButton = itemView.findViewById(R.id.post_reply)

    fun bindCommentReply(comment: Comment) {
        commentVotes.text = comment.upvotesCount.toString()
        commentVotes.isActivated = comment.upvoted != null && comment.upvoted
    }
}
