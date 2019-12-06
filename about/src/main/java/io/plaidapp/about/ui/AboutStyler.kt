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

package io.plaidapp.about.ui

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import io.plaidapp.core.R as coreR
import io.plaidapp.core.util.ColorUtils
import javax.inject.Inject

/**
 * Provide style colors to links and highlights in [AboutActivity].
 */
class AboutStyler @Inject constructor(activity: AboutActivity) {

    val linksColor: ColorStateList = AppCompatResources.getColorStateList(
        activity,
        coreR.color.plaid_links
    )

    @ColorInt
    val highlightColor = ColorUtils.getThemeColor(
        activity,
        coreR.attr.colorPrimary
    )
}
