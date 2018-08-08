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

import android.app.Activity
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.N
import android.support.annotation.ColorInt
import android.support.v7.content.res.AppCompatResources
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.dribbble.R
import io.plaidapp.core.R as coreR

class ShotStyler(activity: Activity) {

    @Suppress("DEPRECATION")
    val locale = if (SDK_INT >= N) {
        activity.resources.configuration.locales[0]
    } else {
        activity.resources.configuration.locale
    }

    val linkColors = AppCompatResources.getColorStateList(activity, R.color.dribbble_links)!!

    @ColorInt val highlightColor = ColorUtils.getThemeColor(
        activity,
        coreR.attr.colorPrimary,
        coreR.color.primary
    )
}