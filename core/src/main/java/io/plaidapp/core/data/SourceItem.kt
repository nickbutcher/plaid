/*
 * Copyright 2019 Google, Inc.
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

package io.plaidapp.core.data

import androidx.annotation.DrawableRes
import io.plaidapp.core.R
import java.util.Comparator
import java.util.Objects

/**
 * Representation of a data source item
 */
abstract class SourceItem(
    val key: String,
    val sortOrder: Int,
    open val name: String,
    @param:DrawableRes @field:DrawableRes val iconRes: Int,
    open var active: Boolean,
    open val isSwipeDismissable: Boolean = false
) {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is SourceItem) return false
        val source = o as SourceItem?
        return sortOrder == source!!.sortOrder &&
            iconRes == source.iconRes &&
            active == source.active &&
            key == source.key &&
            name == source.name
    }

    override fun hashCode(): Int {
        return Objects.hash(key, sortOrder, name, iconRes, active)
    }

    override fun toString(): String {
        return "SourceItem{" +
            "key='" + key + '\''.toString() +
            ", name='" + name + '\''.toString() +
            ", active=" + active +
            '}'.toString()
    }

    open class DesignerNewsSource(
        key: String,
        sortOrder: Int,
        name: String,
        active: Boolean
    ) : SourceItem(key, sortOrder, name, R.drawable.ic_designer_news, active)

    class DesignerNewsSearchSource(
        val query: String,
        active: Boolean
    ) : DesignerNewsSource(
        DESIGNER_NEWS_QUERY_PREFIX + query,
        SEARCH_SORT_ORDER,
        "“$query”",
        active
    ) {

        override val isSwipeDismissable: Boolean
            get() = true

        companion object {

            const val DESIGNER_NEWS_QUERY_PREFIX = "DESIGNER_NEWS_QUERY_"
            private const val SEARCH_SORT_ORDER = 200
        }
    }

    class SourceComparator : Comparator<SourceItem> {

        override fun compare(lhs: SourceItem, rhs: SourceItem): Int {
            return lhs.sortOrder - rhs.sortOrder
        }
    }
}
