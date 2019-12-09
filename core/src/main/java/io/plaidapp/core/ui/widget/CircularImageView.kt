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

package io.plaidapp.core.ui.widget

import android.content.Context
import android.util.AttributeSet
import io.plaidapp.core.util.ViewUtils

/**
 * An extension to image view that has a circular outline.
 */
class CircularImageView(
    context: Context,
    attrs: AttributeSet
) : ForegroundImageView(context, attrs) {

    init {
        outlineProvider = ViewUtils.CIRCULAR_OUTLINE
        clipToOutline = true
    }
}
