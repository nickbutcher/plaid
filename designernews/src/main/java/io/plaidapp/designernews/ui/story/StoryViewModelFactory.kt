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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.designernews.domain.GetCommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.PostStoryCommentUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase

/**
 * Factory for creating [StoryViewModel] with args.
 */
class StoryViewModelFactory(
    private val storyId: Long,
    private val getStoryUseCase: GetStoryUseCase,
    private var postStoryComment: PostStoryCommentUseCase,
    private var postReply: PostReplyUseCase,
    private val getCommentsWithRepliesAndUsersUseCase: GetCommentsWithRepliesAndUsersUseCase,
    private val upvoteStoryUseCase: UpvoteStoryUseCase,
    private val upvoteCommentUseCase: UpvoteCommentUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass != StoryViewModel::class.java) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return StoryViewModel(
            storyId,
            getStoryUseCase,
            postStoryComment,
            postReply,
            getCommentsWithRepliesAndUsersUseCase,
            upvoteStoryUseCase,
            upvoteCommentUseCase,
            dispatcherProvider
        ) as T
    }
}
