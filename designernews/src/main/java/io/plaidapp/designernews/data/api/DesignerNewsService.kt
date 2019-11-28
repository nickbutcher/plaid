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

package io.plaidapp.designernews.data.api

import io.plaidapp.core.data.api.EnvelopePayload
import io.plaidapp.core.designernews.data.users.model.User
import io.plaidapp.designernews.data.comments.model.CommentResponse
import io.plaidapp.designernews.data.comments.model.NewCommentRequest
import io.plaidapp.designernews.data.comments.model.PostCommentResponse
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
    suspend fun getUsers(@Path("ids") userids: String): Response<List<User>>

    @EnvelopePayload("comments")
    @GET("api/v2/comments/{ids}")
    suspend fun getComments(@Path("ids") commentIds: String): Response<List<CommentResponse>>

    @Headers("Content-Type: application/vnd.api+json")
    @POST("api/v2/comments")
    suspend fun comment(@Body comment: NewCommentRequest): Response<PostCommentResponse>

    companion object {
        const val ENDPOINT = "https://www.designernews.co/"
    }
}
