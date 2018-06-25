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

@file:JvmName("DribbbleV1SourceRemover")

package io.plaidapp.base.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * When Dribbble updated from v1 to v2 of their API, they removed a number of data sources. This
 * file checks/removes data source keys from [SharedPreferences] referring to any of the removed
 * API sources.
 */

private const val V1_SOURCE_KEY_PREFIX = "SOURCE_DRIBBBLE_"

fun checkAndRemove(key: String, prefs: SharedPreferences): Boolean {
    var removed = false
    if (isDribbbleV1Source(key)) {
        prefs.edit { remove(key) }
        removed = true
    }
    return removed
}

fun isDribbbleV1Source(key: String) = key.startsWith(V1_SOURCE_KEY_PREFIX)
