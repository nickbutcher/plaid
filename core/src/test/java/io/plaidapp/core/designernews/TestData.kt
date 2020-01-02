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

package io.plaidapp.core.designernews

import io.plaidapp.core.designernews.data.stories.model.StoryLinks
import io.plaidapp.core.designernews.data.users.model.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Test data
 */

val user = User(
    id = 111L,
    firstName = "Plaicent",
    lastName = "van Plaid",
    displayName = "Plaicent van Plaid",
    portraitUrl = "www"
)

val errorResponseBody = "Error".toResponseBody("".toMediaTypeOrNull())

const val userId = 123L

val storyLinks = StoryLinks(
    user = userId,
    comments = listOf(1, 2, 3),
    upvotes = listOf(11, 22, 33),
    downvotes = listOf(111, 222, 333)
)
