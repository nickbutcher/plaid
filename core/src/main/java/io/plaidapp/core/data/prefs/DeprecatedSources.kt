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
