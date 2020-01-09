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

package io.plaidapp.dribbble.domain

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.HtmlParser
import io.plaidapp.dribbble.ui.shot.ShotStyler
import io.plaidapp.dribbble.ui.shot.ShotUiModel
import java.text.NumberFormat
import javax.inject.Inject
import kotlinx.coroutines.withContext

class CreateShotUiModelUseCase @Inject constructor(
    private val htmlParser: HtmlParser,
    private val styler: ShotStyler,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {

    suspend operator fun invoke(model: Shot): ShotUiModel = withContext(dispatcherProvider.computation) {

        val desc = htmlParser.parse(model.description, styler.linkColors, styler.highlightColor)
        val numberFormatter = NumberFormat.getInstance(styler.locale)

        return@withContext ShotUiModel(
            id = model.id,
            title = model.title,
            url = model.htmlUrl,
            formattedDescription = desc,
            imageUrl = model.images.best(),
            imageSize = model.images.bestSize(),
            likesCount = model.likesCount,
            formattedLikesCount = numberFormatter.format(model.likesCount),
            viewsCount = model.viewsCount,
            formattedViewsCount = numberFormatter.format(model.viewsCount),
            createdAt = model.createdAt,
            userName = model.user.name.toLowerCase(),
            userAvatarUrl = model.user.highQualityAvatarUrl
        )
    }
}
