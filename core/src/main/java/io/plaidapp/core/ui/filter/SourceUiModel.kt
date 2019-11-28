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

package io.plaidapp.core.ui.filter

import androidx.annotation.DrawableRes
import io.plaidapp.core.util.event.Event

/**
 * UI model for a source
 */
data class SourceUiModel(
    val id: String,
    val key: String,
    val name: String,
    val active: Boolean,
    @DrawableRes val iconRes: Int,
    val isSwipeDismissable: Boolean,
    val onSourceClicked: (source: SourceUiModel) -> Unit,
    val onSourceDismissed: (source: SourceUiModel) -> Unit
)

/**
 * UI model for the entire list of sources. Contains the data to be displayed and an event that
 * tells whether some sources need to be highlighted or not
 */
data class SourcesUiModel(
    val sourceUiModels: List<SourceUiModel>,
    val highlightSources: Event<SourcesHighlightUiModel>? = null
)

/**
 * UI model for the sources that need to be highlighted. Contains a list of positions that need
 * to be highlighted and the position to which we need to scroll to.
 */
data class SourcesHighlightUiModel(
    val highlightPositions: List<Int>,
    val scrollToPosition: Int
)
