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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.util.exhaustive
import io.plaidapp.designernews.domain.GetCommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.PostStoryCommentUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [ViewModel] responsible for providing data for [StoryActivity] and for handling user actions.
 *  TODO replace the mix of result and coroutines with events.
 */
class StoryViewModel(
    storyId: Long,
    getStoryUseCase: GetStoryUseCase,
    private var postStoryComment: PostStoryCommentUseCase,
    private var postReply: PostReplyUseCase,
    private val getCommentsWithRepliesAndUsers: GetCommentsWithRepliesAndUsersUseCase,
    private val upvoteStory: UpvoteStoryUseCase,
    private val upvoteComment: UpvoteCommentUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    private val _uiModel = MutableLiveData<StoryUiModel>()
    val uiModel: LiveData<StoryUiModel>
        get() = _uiModel

    val story: Story

    init {
        val result = getStoryUseCase(storyId)
        when (result) {
            is Result.Success -> {
                story = result.data
                getComments()
            }
            is Result.Error -> throw result.exception
        }.exhaustive
    }

    private val parentJob = Job()

    fun storyUpvoteRequested(storyId: Long, onResult: (result: Result<Unit>) -> Unit) = launch(
        context = contextProvider.io,
        parent = parentJob
    ) {
        val result = upvoteStory(storyId)
        withContext(contextProvider.io) { onResult(result) }
    }

    fun commentUpvoteRequested(commentId: Long, onResult: (result: Result<Unit>) -> Unit) = launch(
        context = contextProvider.io,
        parent = parentJob
    ) {
        val result = upvoteComment(commentId)
        withContext(contextProvider.io) { onResult(result) }
    }

    fun commentReplyRequested(
        text: CharSequence,
        commentId: Long,
        onResult: (result: Result<Comment>) -> Unit
    ) = launch(contextProvider.io, parent = parentJob) {
        val result = postReply(text.toString(), commentId)
        withContext(contextProvider.main) { onResult(result) }
    }

    fun storyReplyRequested(
        text: CharSequence,
        onResult: (result: Result<Comment>) -> Unit
    ) = launch(contextProvider.io, parent = parentJob) {
        val result = postStoryComment(text.toString(), story.id)
        withContext(contextProvider.main) { onResult(result) }
    }

    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }

    private fun getComments() = launch(contextProvider.io, parent = parentJob) {
        val result = getCommentsWithRepliesAndUsers(story.links.comments)
        if (result is Result.Success) {
            emitUiModel(result.data)
        }
    }

    private fun emitUiModel(comments: List<Comment>) =
        launch(contextProvider.main, parent = parentJob) {
            _uiModel.value = StoryUiModel(comments)
        }
}

/**
 * UI Model for [StoryActivity].
 * TODO update to hold the entire story
 */
data class StoryUiModel(
    val comments: List<Comment>
)
