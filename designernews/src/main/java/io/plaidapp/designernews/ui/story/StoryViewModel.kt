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

import android.arch.lifecycle.ViewModel
import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.Result
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
    private val upvoteStoryUseCase: UpvoteStoryUseCase,
    private val upvoteCommentUseCase: UpvoteCommentUseCase,
    private val contextProvider: CoroutinesContextProvider
) : ViewModel() {

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
}
