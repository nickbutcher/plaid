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

package io.plaidapp.dribbble.ui.shot

import io.plaidapp.core.dribbble.data.api.model.Shot
import java.util.Date

data class ShotUiModel(
    val id: Long,
    val title: String,
    val url: String,
    val description: String,
    val formattedDescription: CharSequence,
    val hideDescription: Boolean,
    val imageUrl: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val viewsCount: Int = 0,
    val formattedViewsCount: String = viewsCount.toString(),
    val likesCount: Int = 0,
    val formattedLikesCount: String = likesCount.toString(),
    val createdAt: Date? = null,
    val userName: String,
    val userAvatarUrl: String
)

/**
 * A sync conversion which skips long running work in order to publish a result asap. For a more
 * complete conversion see [io.plaidapp.dribbble.domain.CreateShotUiModelUseCase].
 */
fun Shot.toShotUiModelSync(): ShotUiModel {
    val (width, height) = images.bestSize()
    return ShotUiModel(
        id = id,
        title = title,
        url = htmlUrl,
        description = description,
        formattedDescription = "",
        hideDescription = false,
        imageUrl = images.best(),
        imageWidth = width,
        imageHeight = height,
        viewsCount = viewsCount,
        formattedViewsCount = viewsCount.toString(),
        likesCount = likesCount,
        formattedLikesCount = likesCount.toString(),
        createdAt = createdAt,
        userName = user.name.toLowerCase(),
        userAvatarUrl = user.highQualityAvatarUrl
    )
}
