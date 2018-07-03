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

package io.plaidapp.core.dribbble.data.prefs

import android.content.Context
import android.support.test.InstrumentationRegistry
import androidx.core.content.edit
import io.plaidapp.core.data.prefs.checkAndRemove
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [DribbbleV1SourceRemover] using shared preferences from instrumentation
 * context.
 */
class DribbbleV1SourceRemoverTest {

    private var sharedPreferences = InstrumentationRegistry.getInstrumentation().context
            .getSharedPreferences("test", Context.MODE_PRIVATE)

    private val dribbbleV1Sources = listOf(
            "SOURCE_DRIBBBLE_POPULAR",
            "SOURCE_DRIBBBLE_FOLLOWING",
            "SOURCE_DRIBBBLE_USER_LIKES",
            "SOURCE_DRIBBBLE_USER_SHOTS",
            "SOURCE_DRIBBBLE_RECENT",
            "SOURCE_DRIBBBLE_DEBUTS",
            "SOURCE_DRIBBBLE_ANIMATED")

    private val nonDribbbleV1Sources = listOf(
            "SOURCE_DESIGNER_NEWS_POPULAR",
            "SOURCE_DESIGNER_NEWS_RECENT",
            "SOURCE_PRODUCT_HUNT",
            "DRIBBBLE_QUERY_FOO",
            "DESIGNER_NEWS_QUERY_BAR")

    @After
    fun tearDown() {
        // Cleanup the shared preferences after every test
        sharedPreferences.edit().clear().commit()
    }

    @Test
    fun checkMixedSources_removesV1SourcesFromSharedPrefs() {
        // Given shared preferences containing a mixture of source keys
        val mixedSourceKeys = dribbbleV1Sources + nonDribbbleV1Sources
        sharedPreferences.edit(commit = true) {
            mixedSourceKeys.forEach { key ->
                putBoolean(key, true)
            }
        }
        // Hold the return value for later verification
        val wasRemoved = BooleanArray(mixedSourceKeys.size)

        // When [DribbbleV1SourceRemover] visits each entry
        mixedSourceKeys.forEachIndexed { index, key ->
            wasRemoved[index] = checkAndRemove(key, sharedPreferences)
        }

        // Then it correctly returns whether it was a v1 source
        wasRemoved.forEachIndexed { index, removed ->
            assertEquals(removed, dribbbleV1Sources.contains(mixedSourceKeys[index]))
        }
        // And removes all v1 sources from [sharedPreferences]
        dribbbleV1Sources.forEach { key ->
            assertFalse(sharedPreferences.contains(key))
        }
        // But leaves all non-v1 sources
        nonDribbbleV1Sources.forEach { key ->
            assertTrue(sharedPreferences.contains(key))
        }
    }
}
