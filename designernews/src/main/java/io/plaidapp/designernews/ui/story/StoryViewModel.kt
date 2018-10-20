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
import io.plaidapp.core.data.CoroutinesDispatcherProvider
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
import kotlinx.coroutines.CoroutineScope
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
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : ViewModel() {

    private val _uiModel = MutableLiveData<StoryUiModel>()
    val uiModel: LiveData<StoryUiModel>
        get() = _uiModel

    val story: Story

    private val parentJob = Job()
    private val scope = CoroutineScope(dispatcherProvider.main + parentJob)

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

    fun storyUpvoteRequested(storyId: Long, onResult: (result: Result<Unit>) -> Unit) =
        scope.launch(dispatcherProvider.computation) {
            val result = upvoteStory(storyId)
            withContext(dispatcherProvider.main) { onResult(result) }
        }

    fun commentUpvoteRequested(commentId: Long, onResult: (result: Result<Unit>) -> Unit) =
        scope.launch(dispatcherProvider.computation) {
            val result = upvoteComment(commentId)
            withContext(dispatcherProvider.main) { onResult(result) }
        }

    fun commentReplyRequested(
        text: CharSequence,
        commentId: Long,
        onResult: (result: Result<Comment>) -> Unit
    ) = scope.launch(dispatcherProvider.computation) {
        val result = postReply(text.toString(), commentId)
        withContext(dispatcherProvider.main) { onResult(result) }
    }

    fun storyReplyRequested(
        text: CharSequence,
        onResult: (result: Result<Comment>) -> Unit
    ) = scope.launch(dispatcherProvider.computation) {
        val result = postStoryComment(text.toString(), story.id)
        withContext(dispatcherProvider.main) { onResult(result) }
    }

    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }

    private fun getComments() = scope.launch(dispatcherProvider.computation) {
        val result = getCommentsWithRepliesAndUsers(story.links.comments)
        if (result is Result.Success) {
            withContext(dispatcherProvider.main) { emitUiModel(result.data) }
        }
    }

    private fun emitUiModel(comments: List<Comment>) {
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
