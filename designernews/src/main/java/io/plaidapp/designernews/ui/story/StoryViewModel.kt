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
import android.support.annotation.StringRes
import android.util.Log
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.core.util.event.Event
import io.plaidapp.core.util.exhaustive
import io.plaidapp.designernews.domain.CommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostCommentUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import io.plaidapp.R as appR

/**
 * [ViewModel] responsible for providing data for [StoryActivity] and for handling user actions.
 *  TODO replace the mix of result and coroutines with events.
 */
class StoryViewModel(
    storyId: Long,
    getStoryUseCase: GetStoryUseCase,
    private var postComment: PostCommentUseCase,
    private var postReply: PostReplyUseCase,
    private val commentsWithRepliesAndUsers: CommentsWithRepliesAndUsersUseCase,
    private val upvoteStoryUseCase: UpvoteStoryUseCase,
    private val upvoteCommentUseCase: UpvoteCommentUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

    private val _showErrorMessage = MutableLiveData<Event<Int>>()
    val showErrorMessage: LiveData<Event<Int>>
        get() = _showErrorMessage

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
        }
    }

    private val parentJob = Job()

    fun commentReplyRequested(text: CharSequence, commentId: Long) =
        launch(contextProvider.io, parent = parentJob) {
            val result = postReply(text, commentId)
            when (result) {
                is Result.Success -> {
                    Log.d("flo", "Reply posted")
                }
                is Result.Error -> showErrorMessage(appR.string.error_posting_reply)
            }.exhaustive
        }

    fun storyReplyRequested(text: CharSequence) =
        launch(contextProvider.io, parent = parentJob) {
            val result = postComment(text.toString(), story.id)
            when (result) {
                is Result.Success -> {
                    Log.d("flo", "Story reply posted")
                }
                is Result.Error -> showErrorMessage(appR.string.error_posting_reply)
            }.exhaustive
        }

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
            val result = commentsWithRepliesAndUsers(it.comments)
            if (result is Result.Success) {
                emitUiModel(result.data)
            }
        }
    }

    private fun emitUiModel(comments: List<Comment>) =
        launch(contextProvider.main, parent = parentJob) {
            _uiModel.value = StoryUiModel(comments)
        }

    private fun showErrorMessage(@StringRes message: Int) =
        launch(contextProvider.main, parent = parentJob) {
            _showErrorMessage.value = Event(message)
        }
}

/**
 * UI Model for [StoryActivity].
 * TODO update to hold the entire story
 */
data class StoryUiModel(
    val comments: List<Comment>
)
