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

package io.plaidapp.core.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.plaidapp.core.R
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_DISABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_ENABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.HIGHLIGHT
import io.plaidapp.core.ui.recyclerview.FilterSwipeDismissListener

private val sourceUiModelDiff = object : DiffUtil.ItemCallback<SourceUiModel>() {
    override fun areItemsTheSame(oldItem: SourceUiModel, newItem: SourceUiModel): Boolean {
        return oldItem.key == newItem.key
    }

    override fun areContentsTheSame(oldItem: SourceUiModel, newItem: SourceUiModel): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: SourceUiModel, newItem: SourceUiModel): Any? {
        if (!oldItem.active && newItem.active) {
            // filter enabled
            return FILTER_ENABLED
        }
        if (oldItem.active && !newItem.active) {
            // filter disabled
            return FILTER_DISABLED
        }
        return null
    }
}

/**
 * Adapter for showing the list of data sources used as filters for the home grid.
 */
class FilterAdapter : ListAdapter<SourceUiModel, FilterViewHolder>(sourceUiModelDiff), FilterSwipeDismissListener {

    init {
        setHasStableIds(true)
    }

    fun highlightPositions(positions: List<Int>) {
        positions.forEach {
            notifyItemChanged(it, HIGHLIGHT)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FilterViewHolder {
        val holder = FilterViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.filter_item, viewGroup, false)
        )
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val uiModel = getItem(position)
            uiModel.onSourceClicked(uiModel)
        }
        return holder
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = getItem(position)
        holder.bind(filter)
    }

    override fun onBindViewHolder(
        holder: FilterViewHolder,
        position: Int,
        partialChangePayloads: List<Any>
    ) {
        if (!partialChangePayloads.isEmpty()) {
            // if we're doing a partial re-bind i.e. an item is enabling/disabling or being
            // highlighted then data hasn't changed. Just set state based on the payload
            val filterEnabled = partialChangePayloads.contains(FILTER_ENABLED)
            val filterDisabled = partialChangePayloads.contains(FILTER_DISABLED)
            if (filterEnabled || filterDisabled) {
                holder.enableFilter(filterEnabled)
                // icon is handled by the animator
            }
            // nothing to do for highlight
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun onItemDismiss(position: Int) {
        val uiModel = getItem(position)
        uiModel.onSourceDismissed(uiModel)
    }
}
