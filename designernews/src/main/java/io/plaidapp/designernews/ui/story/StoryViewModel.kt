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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.designernews.domain.CommentsUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

/**
 * [ViewModel] responsible for providing data for [StoryActivity] and for handling user actions.
 *  TODO replace the mix of result and coroutines with events.
 */
class StoryViewModel(
    storyId: Long,
    getStoryUseCase: GetStoryUseCase,
    private val commentsUseCase: CommentsUseCase,
    private val upvoteStoryUseCase: UpvoteStoryUseCase,
    private val upvoteCommentUseCase: UpvoteCommentUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    private val _uiState = MutableLiveData<StoryUiModel>()
    val uiState: LiveData<StoryUiModel>
        get() = _uiState

    val story: Story

    init {
        val result = getStoryUseCase(storyId)
        when (result) {
            is Result.Success -> {
                story = result.data
                getComments()
            }
            is Result.Error -> throw result.exception
        }
    }

    private val parentJob = Job()

    fun storyUpvoteRequested(storyId: Long, onResult: (result: Result<Unit>) -> Unit) = launch(
        context = contextProvider.io,
        parent = parentJob
    ) {
        val result = upvoteStoryUseCase.upvoteStory(storyId)
        withContext(contextProvider.io) { onResult(result) }
    }

    fun commentUpvoteRequested(commentId: Long, onResult: (result: Result<Unit>) -> Unit) = launch(
        context = contextProvider.io,
        parent = parentJob
    ) {
        val result = upvoteCommentUseCase.upvoteComment(commentId)
        withContext(contextProvider.io) { onResult(result) }
    }

    override fun onCleared() {
        parentJob.cancel()
        super.onCleared()
    }

    private fun getComments() = launch(contextProvider.io, parent = parentJob) {
        story.links?.let {
            val result = commentsUseCase(it.comments)
            if (result is Result.Success) {
                emitUiModel(result.data)
            }
        }
    }

    private fun emitUiModel(comments: List<Comment>) =
        launch(contextProvider.main, parent = parentJob) {
            _uiState.value = StoryUiModel(comments)
        }
}

/**
 * UI Model for [StoryActivity].
 * TODO update to hold the entire story
 */
data class StoryUiModel(
    val comments: List<Comment>
)
