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

@file:JvmName("PlaidItemsList")

package io.plaidapp.core.ui

import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.PlaidItemSorting
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.domain.StoryWeigher
import io.plaidapp.core.dribbble.data.api.ShotWeigher
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem.Companion.SOURCE_PRODUCT_HUNT
import io.plaidapp.core.producthunt.data.api.PostWeigher
import io.plaidapp.core.producthunt.data.api.model.Post
import java.util.Collections

/**
 * Prepares items for display of de-duplicating items and sorting them (depending on the data
 * source).
 */

fun getPlaidItemsForDisplayExpanded(
    oldItems: List<PlaidItem>,
    newItems: List<PlaidItem>,
    columns: Int
): List<PlaidItem> {
    val itemsToBeDisplayed = getPlaidItemsForDisplay(oldItems, newItems)
    expandPopularItems(itemsToBeDisplayed, columns)
    return itemsToBeDisplayed
}

fun getPlaidItemsForDisplay(
    oldItems: List<PlaidItem>,
    newItems: List<PlaidItem>
): List<PlaidItem> {
    val itemsToBeDisplayed = oldItems.toMutableList()
    weighItems(newItems.toMutableList())
    deduplicateAndAdd(itemsToBeDisplayed, newItems)
    sort(itemsToBeDisplayed)
    return itemsToBeDisplayed
}

fun expandPopularItems(items: List<PlaidItem>, columns: Int) {
    // for now just expand the first dribbble image per page which should be
    // the most popular according to our weighing & sorting
    val expandedPositions = mutableListOf<Int>()
    var page = -1
    items.forEachIndexed { index, item ->
        if (item is Shot && item.page > page) {
            item.colspan = columns
            page = item.page
            expandedPositions.add(index)
        } else {
            item.colspan = 1
        }
    }

    // make sure that any expanded items are at the start of a row
    // so that we don't leave any gaps in the grid
    expandedPositions.indices.forEach { expandedPos ->
        val pos = expandedPositions[expandedPos]
        val extraSpannedSpaces = expandedPos * (columns - 1)
        val rowPosition = (pos + extraSpannedSpaces) % columns
        if (rowPosition != 0) {
            val swapWith = pos + (columns - rowPosition)
            if (swapWith < items.size) {
                Collections.swap(items, pos, swapWith)
            }
        }
    }
}

/**
 * Calculate a 'weight' [0, 1] for each data type for sorting. Each data type/source has a
 * different metric for weighing it e.g. Dribbble uses likes etc. but some sources should keep
 * the order returned by the API. Weights are 'scoped' to the page they belong to and lower
 * weights are sorted earlier in the grid (i.e. in ascending weight).
 */
private fun weighItems(items: MutableList<out PlaidItem>?) {
    if (items == null || items.isEmpty()) return

    // some sources should just use the natural order i.e. as returned by the API as users
    // have an expectation about the order they appear in
    items.filter { SOURCE_PRODUCT_HUNT == it.dataSource }.apply {
        PlaidItemSorting.NaturalOrderWeigher().weigh(this)
    }

    // otherwise use our own weight calculation. We prefer this as it leads to a less
    // regular pattern of items in the grid
    items.filterIsInstance<Shot>().apply {
        ShotWeigher().weigh(this)
    }
    items.filterIsInstance<Story>().apply {
        StoryWeigher().weigh(this)
    }
    items.filter { it is Post && SOURCE_PRODUCT_HUNT != it.dataSource }.apply {
        @Suppress("UNCHECKED_CAST")
        PostWeigher().weigh(this as List<Post>)
    }
}

/**
 * De-dupe as the same item can be returned by multiple feeds
 */
private fun deduplicateAndAdd(oldItems: MutableList<PlaidItem>, newItems: List<PlaidItem>) {
    val count = oldItems.count()
    newItems.forEach { newItem ->
        var add = true
        for (i in 0 until count) {
            val existingItem = oldItems[i]
            if (newItem == existingItem) {
                add = false
                break
            }
        }
        if (add) {
            oldItems.add(newItem)
        }
    }
}

private fun sort(items: MutableList<out PlaidItem>) {
    Collections.sort<PlaidItem>(items, PlaidItemSorting.PlaidItemComparator()) // sort by weight
}
