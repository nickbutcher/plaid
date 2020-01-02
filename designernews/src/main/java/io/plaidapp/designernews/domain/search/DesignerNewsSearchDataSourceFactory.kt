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

package io.plaidapp.designernews.domain.search

import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.interfaces.PlaidDataSource
import io.plaidapp.core.interfaces.SearchDataSourceFactory

/**
 * Factory for Designer News sources that can be searched
 */
class DesignerNewsSearchDataSourceFactory(
    private val repository: StoriesRepository
) : SearchDataSourceFactory {

    override fun create(query: String): PlaidDataSource {
        val sourceItem = DesignerNewsSearchSourceItem(query)
        return DesignerNewsDataSource(sourceItem, repository)
    }
}
