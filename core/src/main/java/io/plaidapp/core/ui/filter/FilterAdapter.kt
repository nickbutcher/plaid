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

package io.plaidapp.core.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.R
import io.plaidapp.core.data.Source
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_DISABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_ENABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.HIGHLIGHT
import io.plaidapp.core.ui.recyclerview.FilterSwipeDismissListener

/**
 * Adapter for showing the list of data sources used as filters for the home grid.
 */
class FilterAdapter(
    private val sourcesRepository: SourcesRepository
) : RecyclerView.Adapter<FilterViewHolder>(), FilterSwipeDismissListener {

    private var callbacks: MutableList<FiltersChangedCallback> = ArrayList()

    val enabledSourcesCount: Int
        get() = sources.count(Source::active)

    private var _sources: MutableList<Source>
    val sources: List<Source>
        get() = _sources

    init {
        setHasStableIds(true)
        _sources = sourcesRepository.getSources().toMutableList()
    }

    fun updateSources(newSources: List<Source>) {
        _sources = newSources.toMutableList()
        notifyDataSetChanged()
    }

    fun activateSource(source: Source) {
        sources.withIndex().find { it.value == source }?.apply {
            value.active = true
            dispatchFiltersChanged(source)
            notifyItemChanged(index, FILTER_ENABLED)
        }
    }

    private fun removeFilter(removing: Source) {
        val position = getFilterPosition(removing)
        _sources.removeAt(position)
        notifyItemRemoved(position)
        dispatchFilterRemoved(removing)
        sourcesRepository.removeSource(removing)
    }

    fun getFilterPosition(filter: Source) = sources.indexOf(filter)

    fun highlightFilter(adapterPosition: Int) {
        notifyItemChanged(adapterPosition, HIGHLIGHT)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FilterViewHolder {
        val holder = FilterViewHolder(
                LayoutInflater.from(viewGroup.context)
                        .inflate(R.layout.filter_item, viewGroup, false)
        )
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            val filter = sources[position]
            filter.active = !filter.active
            holder.enableFilter(filter.active)
            notifyItemChanged(
                    position, if (filter.active) {
                FILTER_ENABLED
            } else {
                FILTER_DISABLED
            }
            )
            sourcesRepository.updateSource(filter)
            dispatchFiltersChanged(filter)
        }
        return holder
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = sources[position]
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

    override fun getItemCount() = sources.size

    override fun getItemId(position: Int): Long {
        return sources[position].key.hashCode().toLong()
    }

    override fun onItemDismiss(position: Int) {
        val removing = sources[position]
        if (removing.isSwipeDismissable) {
            removeFilter(removing)
        }
    }

    fun registerFilterChangedCallback(callback: FiltersChangedCallback) {
        callbacks.add(callback)
    }

    fun dispatchFiltersChanged(filter: Source) {
        for (callback in callbacks) {
            callback.onFiltersChanged(filter)
        }
    }

    private fun dispatchFilterRemoved(filter: Source) {
        for (callback in callbacks) {
            callback.onFilterRemoved(filter)
        }
    }

    companion object {

        internal const val FILTER_ICON_ENABLED_ALPHA = 179 // 70%
        internal const val FILTER_ICON_DISABLED_ALPHA = 51 // 20%

        @Volatile
        private var INSTANCE: FilterAdapter? = null

        fun getInstance(sourcesRepository: SourcesRepository): FilterAdapter {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FilterAdapter(sourcesRepository).also { INSTANCE = it }
            }
        }
    }
}
