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

package io.plaidapp.dribbble.ui.shot

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import java.util.Date

/**
 * Shot model for the UI
 */
data class ShotUiModel(
    val id: Long,
    val title: String,
    val url: String,
    val formattedDescription: CharSequence,
    val imageUrl: String,
    val imageSize: Images.ImageSize,
    val likesCount: Int,
    val formattedLikesCount: String,
    val viewsCount: Int,
    val formattedViewsCount: String,
    val createdAt: Date?,
    val userName: String,
    val userAvatarUrl: String
) {
    val shouldHideDescription = formattedDescription.isEmpty()
}

/**
 * A sync conversion which skips long running work in order to publish a result asap. For a more
 * complete conversion see [io.plaidapp.dribbble.domain.CreateShotUiModelUseCase].
 */
fun Shot.toShotUiModel(): ShotUiModel {
    return ShotUiModel(
        id = id,
        title = title,
        url = htmlUrl,
        formattedDescription = "",
        imageUrl = images.best(),
        imageSize = images.bestSize(),
        likesCount = likesCount,
        formattedLikesCount = likesCount.toString(),
        viewsCount = viewsCount,
        formattedViewsCount = viewsCount.toString(),
        createdAt = createdAt,
        userName = user.name.toLowerCase(),
        userAvatarUrl = user.highQualityAvatarUrl
    )
}
