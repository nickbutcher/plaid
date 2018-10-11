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

package io.plaidapp.core.designernews.data.api

import io.plaidapp.core.data.api.EnvelopePayload
import io.plaidapp.core.designernews.data.comments.model.CommentResponse
import io.plaidapp.core.designernews.data.comments.model.NewCommentRequest
import io.plaidapp.core.designernews.data.comments.model.PostCommentResponse
import io.plaidapp.core.designernews.data.login.model.AccessToken
import io.plaidapp.core.designernews.data.login.model.LoggedInUserResponse
import io.plaidapp.core.designernews.data.poststory.model.NewStoryRequest
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.core.designernews.data.votes.model.UpvoteCommentRequest
import io.plaidapp.core.designernews.data.votes.model.UpvoteStoryRequest
import io.plaidapp.core.designernews.domain.model.Comment
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Models the Designer News API.
 *
 * v1 docs: https://github.com/layervault/dn_api
 * v2 docs: https://github.com/DesignerNews/dn_api_v2
 */
interface DesignerNewsService {

    @EnvelopePayload("stories")
    @GET("api/v2/stories")
    fun getStories(@Query("page") page: Int?): Deferred<Response<List<StoryResponse>>>

    @EnvelopePayload("stories")
    @GET("api/v1/stories/search")
    fun search(
        @Query("query") query: String,
        @Query("page") page: Int?
    ): Deferred<Response<List<StoryResponse>>>

    @EnvelopePayload("users")
    @GET("api/v2/users/{ids}")
    fun getUsers(@Path("ids") userids: String): Deferred<Response<List<User>>>

    @EnvelopePayload("users")
    @GET("api/v2/me")
    fun getAuthedUser(): Deferred<Response<List<LoggedInUserResponse>>>

    @FormUrlEncoded
    @POST("oauth/token")
    fun login(@FieldMap loginParams: Map<String, String>): Deferred<Response<AccessToken>>

    @EnvelopePayload("story")
    @POST("api/v2/stories/{id}/upvote")
    fun upvoteStory(@Path("id") storyId: Long): Call<Story>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/upvotes")
    fun upvoteStoryV2(@Body request: UpvoteStoryRequest): Deferred<Response<Unit>>

    @EnvelopePayload("stories")
    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/stories")
    fun postStory(@Body story: NewStoryRequest): Call<List<Story>>

    @EnvelopePayload("comments")
    @GET("api/v2/comments/{ids}")
    fun getComments(@Path("ids") commentIds: String): Deferred<Response<List<CommentResponse>>>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/comments")
    fun comment(@Body comment: NewCommentRequest): Deferred<Response<PostCommentResponse>>

    @FormUrlEncoded
    @POST("api/v1/comments/{id}/reply")
    fun replyToComment(
        @Path("id") commentId: Long,
        @Field("comment[body]") comment: String
    ): Call<Comment>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/comment_upvotes")
    fun upvoteComment(@Body request: UpvoteCommentRequest): Deferred<Response<Unit>>

    companion object {
        const val ENDPOINT = "https://www.designernews.co/"
    }
}
