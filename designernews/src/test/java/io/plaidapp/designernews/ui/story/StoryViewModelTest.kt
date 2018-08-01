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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [StoryViewModel] mocking all the dependencies.
 */
class StoryViewModelTest {

    private val storyId = 1345L
    private val commentId = 999L
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val testStory =
        Story(id = storyId, title = "Plaid 2.0 was released", createdAt = createdDate)

    private val getStoryUseCase: GetStoryUseCase = mock()
    private val upvoteStoryUseCase: UpvoteStoryUseCase = mock()
    private val upvoteCommentUseCase: UpvoteCommentUseCase = mock()

    @Test
    fun loadStory_existsInRepo() {
        // Given that the repo successfully returns the requested story
        // When the view model is constructed
        val viewModel = withViewModel()

        // Then the story is present
        assertNotNull(viewModel.story)
    }

    @Test(expected = IllegalStateException::class)
    fun loadStory_notInRepo() {
        // Given that the repo fails to return the requested story
        whenever(getStoryUseCase(storyId)).thenReturn(Result.Error(Exception()))

        // When the view model is constructed
        StoryViewModel(
            storyId,
            getStoryUseCase,
            upvoteStoryUseCase,
            upvoteCommentUseCase,
            provideFakeCoroutinesContextProvider()
        )
        // Then it throws
    }

    @Test
    fun upvoteStory_whenUpvoteSuccessful() = runBlocking {
        // Given that the use case responds with success
        whenever(upvoteStoryUseCase.upvoteStory(storyId)).thenReturn(Result.Success(Unit))
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Unit>? = null

        // When upvoting a story
        viewModel.storyUpvoteRequested(storyId) { result = it }

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteStory_whenUpvoteFailed() = runBlocking {
        // Given that the use case responds with error
        val response = Result.Error(IOException("Error upvoting"))
        whenever(upvoteStoryUseCase.upvoteStory(storyId)).thenReturn(response)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Unit>? = null

        // When upvoting a story
        viewModel.storyUpvoteRequested(storyId) { result = it }

        // Then the result is an error
        assertTrue(result is Result.Error)
    }

    @Test
    fun upvoteComment_whenUpvoteSuccessful() = runBlocking {
        // Given that the use case responds with success
        whenever(upvoteCommentUseCase.upvoteComment(commentId))
            .thenReturn(Result.Success(Unit))
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Unit>? = null

        // When upvoting a comment
        viewModel.commentUpvoteRequested(commentId) { result = it }

        // Then the result is successful
        assertEquals(Result.Success(Unit), result)
    }

    @Test
    fun upvoteComment_whenUpvoteFailed() = runBlocking {
        // Given that the use case responds with error
        val response = Result.Error(IOException("Error upvoting"))
        whenever(upvoteCommentUseCase.upvoteComment(commentId)).thenReturn(response)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Unit>? = null

        // When upvoting a comment
        viewModel.commentUpvoteRequested(commentId) { result = it }

        // Then the result is an error
        assertTrue(result is Result.Error)
    }

    private fun withViewModel(): StoryViewModel {
        whenever(getStoryUseCase(storyId)).thenReturn(Result.Success(testStory))
        return StoryViewModel(
            storyId,
            getStoryUseCase,
            upvoteStoryUseCase,
            upvoteCommentUseCase,
            provideFakeCoroutinesContextProvider()
        )
    }
}
