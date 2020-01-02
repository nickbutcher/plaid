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

package io.plaidapp.core.data.prefs

import android.content.SharedPreferences

/**
 * When DesignerNews updated from v1 to v2 of their API, they removed the recent data source. This
 * file checks/removes the data source key from [SharedPreferences].
 */

private const val DEPRECATED_SOURCE_DESIGNER_NEWS_RECENT = "SOURCE_DESIGNER_NEWS_RECENT"

fun isDeprecatedDesignerNewsSource(key: String) = key == DEPRECATED_SOURCE_DESIGNER_NEWS_RECENT

/**
 * When Dribbble updated from v1 to v2 of their API, they removed a number of data sources. This
 * file checks/removes data source keys from [SharedPreferences] referring to any of the removed
 * API sources.
 */

private const val DEPRECATED_V1_SOURCE_KEY_PREFIX = "SOURCE_DRIBBBLE_"

fun isDeprecatedDribbbleV1Source(key: String) = key.startsWith(DEPRECATED_V1_SOURCE_KEY_PREFIX)
