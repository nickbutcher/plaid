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

package io.plaidapp.core.designernews.data.api

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for [DesignerNewsSearchConverter] using static test data
 */
class DesignerNewsSearchConverterTest {

    @Test
    fun parsesHtml() {
        // Given a response from a Designer News search
        val response = loadData("/designernews_search.html").toResponseBody(null)

        // When we convert this
        val searchResults = DesignerNewsSearchConverter.convert(response)

        // Then the html is parsed into a list of search results
        assertEquals(6, searchResults.size)

        // Then the ID is extracted correctly
        assertEquals("68181", searchResults[0])
    }

    private fun loadData(path: String): String {
        val inputStream = DesignerNewsSearchConverterTest::class.java.getResourceAsStream(path)!!
        return inputStream.bufferedReader().use { it.readText() }
    }
}
