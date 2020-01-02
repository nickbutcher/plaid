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

package io.plaidapp.core.designernews.data

import io.plaidapp.core.R
import io.plaidapp.core.data.SourceItem

open class DesignerNewsSourceItem(
    id: String,
    key: String,
    sortOrder: Int,
    name: String,
    active: Boolean
) : SourceItem(id, key, sortOrder, name, R.drawable.ic_designer_news, active, true)

data class DesignerNewsSearchSourceItem(
    val query: String,
    override var active: Boolean = true
) : DesignerNewsSourceItem(
    DESIGNER_NEWS_QUERY_PREFIX + query,
    query,
    SEARCH_SORT_ORDER,
    "“$query”",
    active
) {

    companion object {
        const val SOURCE_DESIGNER_NEWS_POPULAR = "SOURCE_DESIGNER_NEWS_POPULAR"
        const val DESIGNER_NEWS_QUERY_PREFIX = "DESIGNER_NEWS_QUERY_"
        private const val SEARCH_SORT_ORDER = 200
    }
}
