/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.producthunt.data

import io.plaidapp.core.producthunt.data.api.model.GetPostItemResponse
import io.plaidapp.core.producthunt.data.api.model.GetPostsResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response

val errorResponseBody = "Error".toResponseBody("".toMediaTypeOrNull())

val post1 = GetPostItemResponse(
    id = 345L,
    url = "www.plaid.amazing",
    name = "Plaid",
    tagline = "amazing",
    discussionUrl = "www.disc.plaid",
    redirectUrl = "www.d.plaid",
    commentsCount = 5,
    votesCount = 100
)
val post2 = GetPostItemResponse(
    id = 947L,
    url = "www.plaid.team",
    name = "Plaid",
    tagline = "team",
    discussionUrl = "www.team.plaid",
    redirectUrl = "www.t.plaid",
    commentsCount = 2,
    votesCount = 42
)

val responseDataSuccess = GetPostsResponse(posts = listOf(post1, post2))

val responseSuccess = Response.success(responseDataSuccess)

val responseError = Response.error<GetPostsResponse>(
    400,
    errorResponseBody
)
