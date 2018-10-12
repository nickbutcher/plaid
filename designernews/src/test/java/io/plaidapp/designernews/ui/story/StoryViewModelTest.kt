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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryLinks
import io.plaidapp.core.designernews.domain.model.Comment
import io.plaidapp.designernews.domain.GetCommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.PostStoryCommentUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import io.plaidapp.designernews.flattendCommentsWithReplies
import io.plaidapp.designernews.reply1
import io.plaidapp.test.shared.LiveDataTestUtil
import io.plaidapp.test.shared.provideFakeCoroutinesContextProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [StoryViewModel] mocking all the dependencies.
 */
class StoryViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val userId = 5L
    private val storyId = 1345L
    private val commentId = 999L
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val commentIds = listOf(11L, 12L)
    private val storyLinks = StoryLinks(
        user = userId,
        comments = commentIds,
        upvotes = emptyList(),
        downvotes = emptyList()
    )
    private val testStory = Story(
        id = storyId,
        title = "Plaid 2.0 was released",
        createdAt = createdDate,
        userId = userId,
        links = storyLinks
    )

    private val getStory: GetStoryUseCase = mock()
    private val postStoryComment: PostStoryCommentUseCase = mock()
    private val postComment: PostReplyUseCase = mock()
    private val getCommentsWithRepliesAndUsers: GetCommentsWithRepliesAndUsersUseCase = mock()
    private val upvoteStory: UpvoteStoryUseCase = mock()
    private val upvoteComment: UpvoteCommentUseCase = mock()

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
        whenever(getStory(storyId)).thenReturn(Result.Error(IllegalStateException()))

        // When the view model is constructed
        StoryViewModel(
            storyId,
            getStory,
            postStoryComment,
            postComment,
            getCommentsWithRepliesAndUsers,
            upvoteStory,
            upvoteComment,
            provideFakeCoroutinesContextProvider()
        )
        // Then it throws
    }

    @Test
    fun commentsRequested_whenViewModelCreated() {
        // Given that the repo successfully returns the requested story
        // When the view model is constructed
        val viewModel = withViewModel()

        // Then the correct UI model is created
        val event = LiveDataTestUtil.getValue(viewModel.uiModel)
        assertEquals(event!!.comments, flattendCommentsWithReplies)
    }

    @Test
    fun upvoteStory_whenUpvoteSuccessful() = runBlocking {
        // Given that the use case responds with success
        whenever(upvoteStory(storyId)).thenReturn(Result.Success(Unit))
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
        whenever(upvoteStory(storyId)).thenReturn(response)
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
        whenever(upvoteComment(commentId))
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
        whenever(upvoteComment(commentId)).thenReturn(response)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Unit>? = null

        // When upvoting a comment
        viewModel.commentUpvoteRequested(commentId) { result = it }

        // Then the result is an error
        assertTrue(result is Result.Error)
    }

    @Test
    fun commentReplyRequested_withSuccess() = runBlocking {
        // Given that the comment reply is posted successfully
        val expected = Result.Success(reply1)
        whenever(postComment.invoke(reply1.body, reply1.parentCommentId!!)).thenReturn(expected)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Comment>? = null

        // When posting a comment reply
        viewModel.commentReplyRequested(reply1.body, reply1.parentCommentId!!) {
            result = it
        }

        // Then the result is the expected one
        assertEquals(expected, result)
    }

    @Test
    fun commentReplyRequested_withError() = runBlocking {
        // Given that the comment reply is posted with error
        val expected = Result.Error(IOException("Error"))
        whenever(postComment.invoke(reply1.body, reply1.parentCommentId!!)).thenReturn(expected)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Comment>? = null

        // When posting a comment reply
        viewModel.commentReplyRequested(reply1.body, reply1.parentCommentId!!) {
            result = it
        }

        // Then the result is the expected one
        assertEquals(expected, result)
    }

    @Test
    fun storyReplyRequested_withSuccess() = runBlocking {
        // Given that the comment reply is posted successfully
        val expected = Result.Success(reply1)
        whenever(postStoryComment.invoke(reply1.body, storyId))
            .thenReturn(expected)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Comment>? = null

        // When posting a comment reply
        viewModel.storyReplyRequested(reply1.body) { result = it }

        // Then the result is the expected one
        assertEquals(expected, result)
    }

    @Test
    fun storyReplyRequested_withError() = runBlocking {
        // Given that the comment reply is posted with error
        val expected = Result.Error(IOException("Error"))
        whenever(postStoryComment.invoke(reply1.body, storyId)).thenReturn(expected)
        // And the view model is constructed
        val viewModel = withViewModel()
        var result: Result<Comment>? = null

        // When posting a comment reply
        viewModel.storyReplyRequested(reply1.body) { result = it }

        // Then the result is the expected one
        assertEquals(expected, result)
    }

    private fun withViewModel(): StoryViewModel {
        whenever(getStory(storyId)).thenReturn(Result.Success(testStory))
        runBlocking {
            whenever(getCommentsWithRepliesAndUsers(commentIds)).thenReturn(
                Result.Success(
                    flattendCommentsWithReplies
                )
            )
        }
        return StoryViewModel(
            storyId,
            getStory,
            postStoryComment,
            postComment,
            getCommentsWithRepliesAndUsers,
            upvoteStory,
            upvoteComment,
            provideFakeCoroutinesContextProvider()
        )
    }
}
