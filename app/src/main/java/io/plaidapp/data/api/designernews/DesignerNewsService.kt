/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.data.api.designernews

import io.plaidapp.data.api.EnvelopePayload
import io.plaidapp.data.api.designernews.model.AccessToken
import io.plaidapp.data.api.designernews.model.Comment
import io.plaidapp.data.api.designernews.model.NewStoryRequest
import io.plaidapp.data.api.designernews.model.Story
import io.plaidapp.data.api.designernews.model.User
import retrofit2.Call
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

    @EnvelopePayload("user")
    @GET("api/v1/me")
    fun getAuthedUser(): Call<User>

    @EnvelopePayload("stories")
    @GET("api/v2/stories")
    fun getTopStoriesV2(@Query("page") page: Int?): Call<List<Story>>

    @EnvelopePayload("stories")
    @GET("api/v2/stories/recent")
    fun getRecentStoriesV2(@Query("page") page: Int?): Call<List<Story>>

    @EnvelopePayload("stories")
    @GET("api/v1/stories")
    fun getTopStories(@Query("page") page: Int?): Call<List<Story>>

    @EnvelopePayload("stories")
    @GET("api/v1/stories/recent")
    fun getRecentStories(@Query("page") page: Int?): Call<List<Story>>

    @EnvelopePayload("stories")
    @GET("api/v1/stories/search")
    fun search(@Query("query") query: String, @Query("page") page: Int?): Call<List<Story>>

    @FormUrlEncoded
    @POST("oauth/token")
    fun login(@FieldMap loginParams: Map<String, String>): Call<AccessToken>

    @EnvelopePayload("story")
    @POST("api/v1/stories/{id}/upvote")
    fun upvoteStory(@Path("id") storyId: Long): Call<Story>

    @EnvelopePayload("stories")
    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/stories")
    fun postStory(@Body story: NewStoryRequest): Call<List<Story>>

    @FormUrlEncoded
    @POST("api/v1/stories/{id}/reply")
    fun comment(@Path("id") storyId: Long,
                @Field("comment[body]") comment: String): Call<Comment>

    @FormUrlEncoded
    @POST("api/v1/comments/{id}/reply")
    fun replyToComment(@Path("id") commentId: Long,
                       @Field("comment[body]") comment: String): Call<Comment>

    @POST("api/v1/comments/{id}/upvote")
    fun upvoteComment(@Path("id") commentId: Long): Call<Comment>

    companion object {
       const val ENDPOINT = "https://www.designernews.co/"
    }

}
