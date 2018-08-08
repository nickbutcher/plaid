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

package io.plaidapp.dribbble.domain

import io.plaidapp.core.util.HtmlParser
import io.plaidapp.dribbble.ui.shot.ShotStyler
import io.plaidapp.dribbble.ui.shot.ShotUiModel
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import java.text.NumberFormat

class CreateShotUiModelUseCase(private val htmlParser: HtmlParser) {

    operator fun invoke(uiModel: ShotUiModel, styler: ShotStyler): Deferred<ShotUiModel> = async {
        val desc = htmlParser(uiModel.description, styler.linkColors, styler.highlightColor)
        val numberFormatter = NumberFormat.getInstance(styler.locale)
        uiModel.copy(
            formattedDescription = desc,
            hideDescription = desc.isBlank(),
            formattedViewsCount = numberFormatter.format(uiModel.viewsCount),
            formattedLikesCount = numberFormatter.format(uiModel.likesCount)
        )
    }
}
