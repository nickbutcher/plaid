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

package io.plaidapp.dribbble

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import io.plaidapp.dribbble.ui.shot.ShotUiModel

/**
 * Dribbble test data
 */

val player = User(
    id = 1L,
    name = "Nick Butcher",
    username = "nickbutcher",
    avatarUrl = "www.prettyplaid.nb"
)

val testShot = Shot(
    id = 1L,
    title = "Foo",
    page = 0,
    description = "Shot Description",
    images = Images(hidpi = "hidpi"),
    user = player,
    viewsCount = 1234,
    likesCount = 5678
)

val testShotUiModel = ShotUiModel(
    id = 1L,
    title = "Foo",
    url = "url",
    formattedDescription = "Description",
    imageUrl = "imageUrl",
    imageSize = Images.ImageSize.NORMAL_IMAGE_SIZE,
    viewsCount = 1234,
    formattedViewsCount = "1,234",
    likesCount = 5678,
    formattedLikesCount = "5,678",
    createdAt = null,
    userName = "username",
    userAvatarUrl = "avatarUrl"
)
