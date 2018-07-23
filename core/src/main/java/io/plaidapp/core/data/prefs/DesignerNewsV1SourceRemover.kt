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

@file:JvmName("DesignerNewsV1SourceRemover")

package io.plaidapp.core.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * When DesignerNews updated from v1 to v2 of their API, they removed the recent data source. This
 * file checks/removes the data source key from [SharedPreferences].
 */

private const val SOURCE_DESIGNER_NEWS_RECENT = "SOURCE_DESIGNER_NEWS_RECENT"

/**
 * Checks if [key] is SOURCE_DESIGNER_NEWS_RECENT data source and if so, removes it from [prefs].
 * @return `true` if [key] is SOURCE_DESIGNER_NEWS_RECENT data source & was removed,
 * otherwise `false`.
 */
fun checkAndRemoveDesignerNewsRecentSource(key: String, prefs: SharedPreferences): Boolean {
    var removed = false
    if (key == SOURCE_DESIGNER_NEWS_RECENT) {
        prefs.edit { remove(key) }
        removed = true
    }
    return removed
}
