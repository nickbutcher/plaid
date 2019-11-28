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

package io.plaidapp.core.designernews.data.api

import io.plaidapp.core.data.api.EnvelopePayload
import io.plaidapp.core.designernews.data.login.model.AccessToken
import io.plaidapp.core.designernews.data.login.model.LoggedInUserResponse
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
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
    suspend fun getStories(@Query("page") page: Int?): Response<List<StoryResponse>>

    @EnvelopePayload("stories")
    @GET("api/v2/stories/{ids}")
    suspend fun getStories(@Path("ids") commaSeparatedIds: String): Response<List<StoryResponse>>

    @EnvelopePayload("users")
    @GET("api/v2/me")
    suspend fun getAuthedUser(): Response<List<LoggedInUserResponse>>

    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun login(@FieldMap loginParams: Map<String, String>): Response<AccessToken>

    /**
     * Search Designer News by scraping website.
     * Returns a list of story IDs
     */
    @DesignerNewsSearch
    @GET("search?t=story")
    suspend fun search(
        @Query("q") query: String,
        @Query("p") page: Int?
    ): Response<List<String>>

    @EnvelopePayload("story")
    @POST("api/v2/stories/{id}/upvote")
    fun upvoteStory(@Path("id") storyId: Long): Call<Story>

    companion object {
        const val ENDPOINT = "https://www.designernews.co/"
    }
}
