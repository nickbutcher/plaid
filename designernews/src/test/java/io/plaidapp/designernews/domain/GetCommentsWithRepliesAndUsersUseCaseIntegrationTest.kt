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

package io.plaidapp.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.comments.CommentsRemoteDataSource
import io.plaidapp.designernews.data.comments.CommentsRepository
import io.plaidapp.designernews.data.comments.model.CommentResponse
import io.plaidapp.designernews.data.users.UserRemoteDataSource
import io.plaidapp.designernews.data.users.UserRepository
import io.plaidapp.designernews.errorResponseBody
import io.plaidapp.designernews.flattendCommentsWithReplies
import io.plaidapp.designernews.flattenedCommentsWithoutReplies
import io.plaidapp.designernews.parentCommentResponse
import io.plaidapp.designernews.repliesResponses
import io.plaidapp.designernews.reply1
import io.plaidapp.designernews.reply1NoUser
import io.plaidapp.designernews.replyResponse1
import io.plaidapp.designernews.user1
import io.plaidapp.designernews.user2
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * Integration test for [GetCommentsWithRepliesAndUsersUseCase] where only the responses from the [DesignerNewsService]
 * are mocked. Everything else uses real implementation.
 */
class GetCommentsWithRepliesAndUsersUseCaseIntegrationTest {
    private val service: DesignerNewsService = mock()
    private val dataSource = CommentsRemoteDataSource(service)
    private val commentsRepository = CommentsRepository(dataSource)
    private val userRepository = UserRepository(UserRemoteDataSource(service))
    private val repository: GetCommentsWithRepliesAndUsersUseCase = GetCommentsWithRepliesAndUsersUseCase(
        GetCommentsWithRepliesUseCase(commentsRepository),
        userRepository
    )

    @Test
    fun getComments_noReplies_whenCommentsAnUserRequestsSuccessful() = runBlocking {
        // Given that the comments request responds with success
        withComments(replyResponse1, "11")
        // Given that the user request responds with success
        withUsers(listOf(user1), "111")

        // When getting the replies
        val result = repository(listOf(11L))

        // Then the correct list of comments was requested from the API
        verify(service).getComments("11")
        // Then the correct list is received
        assertEquals(Result.Success(listOf(reply1)), result)
    }

    @Test
    fun getComments_noReplies_whenCommentsRequestFailed() = runBlocking {
        // Given that the service responds with failure
        val apiResult = Response.error<List<CommentResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.getComments("11")).thenReturn(apiResult)

        // When getting the comments
        val result = repository(listOf(11L))

        // Then the result is not successful
        assertNotNull(result)
        assertTrue(result is Result.Error)
    }

    @Test
    fun getComments_multipleReplies_whenCommentsAndUsersRequestsSuccessful() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from service we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        withComments(parentCommentResponse, "1")
        // When requesting replies for ids 11 and 12 from service we get the children
        withComments(repliesResponses, "11,12")
        // When the user request responds with success
        withUsers(listOf(user1, user2), "222,111")

        // When getting the comments from the repository
        val result = repository(listOf(1L))

        // Then  API requests were triggered
        verify(service).getComments("1")
        verify(service).getComments("11,12")
        verify(service).getUsers("222,111")
        // Then the correct result is received
        assertEquals(Result.Success(flattendCommentsWithReplies), result)
    }

    @Test
    fun getComments_multipleReplies_whenRepliesRequestFailed() = runBlocking {
        // Given that
        // When requesting replies for ids 1 from service we get the parent comment
        withComments(parentCommentResponse, "1")
        // When requesting replies for ids 11 and 12 from service we get an error
        val resultChildrenError = Response.error<List<CommentResponse>>(
            400,
            errorResponseBody
        )
        whenever(service.getComments("11,12")).thenReturn(resultChildrenError)
        // Given that the user request responds with success
        withUsers(listOf(user2), "222")

        // When getting the comments from the repository
        val result = repository(listOf(1L))

        // Then  API requests were triggered
        verify(service).getComments("1")
        verify(service).getComments("11,12")
        verify(service).getUsers("222")
        // Then the correct result is received
        assertEquals(Result.Success(flattenedCommentsWithoutReplies), result)
    }

    @Test
    fun getComments_whenUserRequestFailed() = runBlocking {
        // Given that:
        // When requesting replies for ids 1 from service we get the parent comment but
        // without replies embedded (since that's what the next call is doing)
        withComments(replyResponse1, "11")
        // Given that the user request responds with failure
        val userError = Response.error<List<User>>(
            400,
            errorResponseBody
        )
        whenever(service.getUsers("111"))
            .thenReturn(userError)

        // When getting the comments from the repository
        val result = repository(listOf(11L))

        // Then  API requests were triggered
        verify(service).getComments("11")
        verify(service).getUsers("111")
        // Then the correct result is received
        assertEquals(Result.Success(arrayListOf(reply1NoUser)), result)
    }

    // Given that the users request responds with success
    private fun withUsers(users: List<User>, ids: String) = runBlocking {
        val userResult = Response.success(users)
        whenever(service.getUsers(ids)).thenReturn(userResult)
    }

    private suspend fun withComments(commentResponse: CommentResponse, ids: String) {
        val resultParent = Response.success(listOf(commentResponse))
        whenever(service.getComments(ids)).thenReturn(resultParent)
    }

    private suspend fun withComments(commentResponse: List<CommentResponse>, ids: String) {
        val resultParent = Response.success(commentResponse)
        whenever(service.getComments(ids)).thenReturn(resultParent)
    }
}
