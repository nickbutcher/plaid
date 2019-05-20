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

package io.plaidapp.designernews.data.api

import io.plaidapp.core.data.api.EnvelopePayload
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.designernews.data.votes.model.UpvoteCommentRequest
import io.plaidapp.designernews.data.votes.model.UpvoteStoryRequest
import io.plaidapp.designernews.data.comments.model.CommentResponse
import io.plaidapp.designernews.data.comments.model.NewCommentRequest
import io.plaidapp.designernews.data.comments.model.PostCommentResponse
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Models the Designer News API.
 *
 * v1 docs: https://github.com/layervault/dn_api
 * v2 docs: https://github.com/DesignerNews/dn_api_v2
 */
interface DesignerNewsService {

    @EnvelopePayload("users")
    @GET("api/v2/users/{ids}")
    fun getUsers(@Path("ids") userids: String): Deferred<Response<List<User>>>

    @EnvelopePayload("story")
    @POST("api/v2/stories/{id}/upvote")
    fun upvoteStory(@Path("id") storyId: Long): Call<Story>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/upvotes")
    fun upvoteStoryV2(@Body request: UpvoteStoryRequest): Deferred<Response<Unit>>

    @EnvelopePayload("comments")
    @GET("api/v2/comments/{ids}")
    fun getComments(@Path("ids") commentIds: String): Deferred<Response<List<CommentResponse>>>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/comments")
    fun comment(@Body comment: NewCommentRequest): Deferred<Response<PostCommentResponse>>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/comment_upvotes")
    fun upvoteComment(@Body request: UpvoteCommentRequest): Deferred<Response<Unit>>

    companion object {
        const val ENDPOINT = "https://www.designernews.co/"
    }
}
