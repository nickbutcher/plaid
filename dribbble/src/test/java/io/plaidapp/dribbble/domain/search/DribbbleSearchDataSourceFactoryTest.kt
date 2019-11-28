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

package io.plaidapp.dribbble.domain.search

import com.nhaarman.mockitokotlin2.mock
import io.plaidapp.core.dribbble.data.ShotsRepository
import org.junit.Assert.assertEquals

/**
 * Test for  [DribbbleSearchDataSourceFactory], mocking dependencies
 */
class DribbbleSearchDataSourceFactoryTest {

    private val repository: ShotsRepository = mock()
    private val factory = DribbbleSearchDataSourceFactory(repository)

    fun create() {
        // Given a query
        val query = "Android"

        // When creating the data source
        val dataSource = factory.create(query)

        // Then the source item has the query as a key
        assertEquals(query, dataSource.sourceItem.key)
    }
}
