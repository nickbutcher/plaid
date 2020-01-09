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

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import io.plaidapp.core.R as coreR
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.dribbble.R
import javax.inject.Inject

/**
 * Decorator for a [ShotUiModel]
 *  - locale is needed to format the views and likes count
 *  - linkColors and highlightColor will define the colors used in the shot description
 */
class ShotStyler @Inject constructor(context: Context) {

    @Suppress("DEPRECATION")
    val locale = if (SDK_INT >= N) {
        context.resources.configuration.locales[0]
    } else {
        context.resources.configuration.locale
    }

    val linkColors = AppCompatResources.getColorStateList(context, R.color.dribbble_links)!!

    @ColorInt
    val highlightColor = ColorUtils.getThemeColor(
        context,
        coreR.attr.colorPrimary
    )
}
