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

package io.plaidapp.core.data

import androidx.annotation.DrawableRes
import java.util.Comparator

/**
 * Representation of a data source item
 */
abstract class SourceItem(
    val id: String,
    val key: String,
    val sortOrder: Int,
    open val name: String,
    @param:DrawableRes @field:DrawableRes val iconRes: Int,
    open var active: Boolean,
    open val isSwipeDismissable: Boolean = false
) {

    override fun toString(): String {
        return "SourceItem{" +
            "key='" + key + '\''.toString() +
            ", name='" + name + '\''.toString() +
            ", active=" + active +
            '}'.toString()
    }

    class SourceComparator : Comparator<SourceItem> {
        override fun compare(lhs: SourceItem, rhs: SourceItem): Int {
            return lhs.sortOrder - rhs.sortOrder
        }
    }
}
