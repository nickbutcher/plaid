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

package io.plaidapp.core.dribbble.data.search

import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for [DribbbleSearchConverter] using static test data
 */
class DribbbleSearchConverterTest {

    @Test
    fun parsesHtml() {
        // Given a response from a dribbble search
        val response = ResponseBody.create(null, loadData("/dribbble_search.html"))

        // When we convert this
        val shots = DribbbleSearchConverter.convert(response)

        // Then the html is parsed into a list of shots
        assertNotNull(shots)
        assertEquals(24, shots.size)

        // Then each shot contains the expected fields
        shots.forEach { shot ->
            assertNotNull(shot)
            assertNotNull(shot.id)
            assertNotNull(shot.title)
            assertNotNull(shot.url)
            assertNotNull(shot.user)
        }
    }

    private fun loadData(path: String): String {
        val inputStream = DribbbleSearchConverterTest::class.java.getResourceAsStream(path)
        return inputStream.bufferedReader().use { it.readText() }
    }
}
