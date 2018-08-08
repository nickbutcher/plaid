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

package io.plaidapp.dribbble

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import io.plaidapp.dribbble.ui.shot.ShotUiModel

/**
 * Dribbble test data
 */

val testPlayer = User(
    id = 1L,
    name = "Nick Butcher",
    username = "nickbutcher",
    avatarUrl = "url"
)

val testShot = Shot(
    id = 1L,
    title = "Foo",
    description = "",
    images = Images(hidpi = "high"),
    user = testPlayer
)

val testUiModel = ShotUiModel(
    id = 1L,
    title = "title",
    url = "url",
    description = "desc",
    formattedDescription = "desc",
    hideDescription = false,
    imageUrl = "imgurl",
    imageWidth = 800,
    imageHeight = 600,
    viewsCount = 12345,
    likesCount = 67890,
    userName = "nickbutcher",
    userAvatarUrl = "avatarurl"
)
