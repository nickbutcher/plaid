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

package io.plaidapp.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import io.plaidapp.core.R
import io.plaidapp.core.data.Source
import io.plaidapp.core.data.prefs.SourceManager
import io.plaidapp.core.ui.recyclerview.FilterSwipeDismissListener
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.ViewUtils
import java.util.Collections

/**
 * Adapter for showing the list of data sources used as filters for the home grid.
 */
class FilterAdapter constructor(
    context: Context,
    val filters: MutableList<Source>
) : RecyclerView.Adapter<FilterViewHolder>(), FilterSwipeDismissListener {

    private val context: Context = context.applicationContext
    private var callbacks: MutableList<FiltersChangedCallbacks> = ArrayList()

    val enabledSourcesCount: Int
        get() = filters.count(Source::active)

    init {
        setHasStableIds(true)
    }

    /**
     * Adds a new data source to the list of filters. If the source already exists then it is simply
     * activated.
     *
     * @param toAdd the source to add
     * @return whether the filter was added (i.e. if it did not already exist)
     */
    fun addFilter(toAdd: Source): Boolean {
        // first check if it already exists
        for (i in 0 until filters.size) {
            val existing = filters[i]
            if (existing.javaClass == toAdd.javaClass &&
                existing.key.equals(toAdd.key, ignoreCase = true)
            ) {
                // already exists, just ensure it's active
                if (!existing.active) {
                    existing.active = true
                    dispatchFiltersChanged(existing)
                    notifyItemChanged(i, FilterAnimator.FILTER_ENABLED)
                    SourceManager.updateSource(existing, context)
                }
                return false
            }
        }
        // didn't already exist, so add it
        filters.add(toAdd)
        Collections.sort(filters, Source.SourceComparator())
        dispatchFiltersChanged(toAdd)
        notifyDataSetChanged()
        SourceManager.addSource(toAdd, context)
        return true
    }

    private fun removeFilter(removing: Source) {
        val position = getFilterPosition(removing)
        filters.removeAt(position)
        notifyItemRemoved(position)
        dispatchFilterRemoved(removing)
        SourceManager.removeSource(removing, context)
    }

    fun getFilterPosition(filter: Source) = filters.indexOf(filter)

    fun enableFilterByKey(key: String, context: Context) {
        val count = filters.size
        for (i in 0 until count) {
            val filter = filters[i]
            if (filter.key == key) {
                if (!filter.active) {
                    filter.active = true
                    notifyItemChanged(i, FilterAnimator.FILTER_ENABLED)
                    dispatchFiltersChanged(filter)
                    SourceManager.updateSource(filter, context)
                }
                return
            }
        }
    }

    fun highlightFilter(adapterPosition: Int) {
        notifyItemChanged(adapterPosition, FilterAnimator.HIGHLIGHT)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): FilterViewHolder {
        val holder = FilterViewHolder(
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.filter_item, viewGroup, false)
        )
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return@setOnClickListener
            val filter = filters[position]
            filter.active = !filter.active
            holder.filterName.isEnabled = filter.active
            notifyItemChanged(
                position, if (filter.active) {
                    FilterAnimator.FILTER_ENABLED
                } else {
                    FilterAnimator.FILTER_DISABLED
                }
            )
            SourceManager.updateSource(filter, holder.itemView.context)
            dispatchFiltersChanged(filter)
        }
        return holder
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        with(holder) {
            isSwipeable = filter.isSwipeDismissable
            filterName.text = filter.name
            filterName.isEnabled = filter.active
            if (filter.iconRes > 0) {
                filterIcon.setImageDrawable(
                    itemView.context.getDrawable(filter.iconRes)
                )
            }
            filterIcon.imageAlpha = if (filter.active)
                FILTER_ICON_ENABLED_ALPHA
            else
                FILTER_ICON_DISABLED_ALPHA
        }
    }

    override fun onBindViewHolder(
        holder: FilterViewHolder,
        position: Int,
        partialChangePayloads: List<Any>
    ) {
        if (!partialChangePayloads.isEmpty()) {
            // if we're doing a partial re-bind i.e. an item is enabling/disabling or being
            // highlighted then data hasn't changed. Just set state based on the payload
            val filterEnabled = partialChangePayloads.contains(FilterAnimator.FILTER_ENABLED)
            val filterDisabled = partialChangePayloads.contains(FilterAnimator.FILTER_DISABLED)
            if (filterEnabled || filterDisabled) {
                holder.filterName.isEnabled = filterEnabled
                // icon is handled by the animator
            }
            // nothing to do for highlight
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount() = filters.size

    override fun getItemId(position: Int): Long {
        return filters[position].key.hashCode().toLong()
    }

    override fun onItemDismiss(position: Int) {
        val removing = filters[position]
        if (removing.isSwipeDismissable) {
            removeFilter(removing)
        }
    }

    fun registerFilterChangedCallback(callback: FiltersChangedCallbacks) {
        callbacks.add(callback)
    }

    fun unregisterFilterChangedCallback(callback: FiltersChangedCallbacks) {
        if (!callbacks.isEmpty()) {
            callbacks.remove(callback)
        }
    }

    private fun dispatchFiltersChanged(filter: Source) {
        for (callback in callbacks) {
            callback.onFiltersChanged(filter)
        }
    }

    private fun dispatchFilterRemoved(filter: Source) {
        for (callback in callbacks) {
            callback.onFilterRemoved(filter)
        }
    }

    abstract class FiltersChangedCallbacks {
        open fun onFiltersChanged(changedFilter: Source) {}

        open fun onFilterRemoved(removed: Source) {}
    }

    companion object {

        internal const val FILTER_ICON_ENABLED_ALPHA = 179 // 70%
        internal const val FILTER_ICON_DISABLED_ALPHA = 51 // 20%
    }
}

/**
 * ViewHolder for filters.
 */
class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var filterName: TextView = itemView.findViewById(R.id.filter_name)
    var filterIcon: ImageView = itemView.findViewById(R.id.filter_icon)
    var isSwipeable: Boolean = false
}

class FilterAnimator : DefaultItemAnimator() {

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun obtainHolderInfo(): RecyclerView.ItemAnimator.ItemHolderInfo {
        return FilterHolderInfo()
    }

    internal class FilterHolderInfo : RecyclerView.ItemAnimator.ItemHolderInfo() {
        var doEnable: Boolean = false
        var doDisable: Boolean = false
        var doHighlight: Boolean = false
    }

    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): RecyclerView.ItemAnimator.ItemHolderInfo {
        val info = super.recordPreLayoutInformation(
            state,
            viewHolder,
            changeFlags,
            payloads
        ) as FilterHolderInfo
        if (!payloads.isEmpty()) {
            info.doEnable = payloads.contains(FILTER_ENABLED)
            info.doDisable = payloads.contains(FILTER_DISABLED)
            info.doHighlight = payloads.contains(HIGHLIGHT)
        }
        return info
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: RecyclerView.ItemAnimator.ItemHolderInfo,
        postInfo: RecyclerView.ItemAnimator.ItemHolderInfo
    ): Boolean {
        if (newHolder is FilterViewHolder && preInfo is FilterHolderInfo) {

            if (preInfo.doEnable || preInfo.doDisable) {
                val iconAlpha = ObjectAnimator.ofInt(
                    newHolder.filterIcon,
                    ViewUtils.IMAGE_ALPHA,
                    if (preInfo.doEnable) {
                        FilterAdapter.FILTER_ICON_ENABLED_ALPHA
                    } else {
                        FilterAdapter.FILTER_ICON_DISABLED_ALPHA
                    }
                )
                iconAlpha.duration = 300L
                iconAlpha.interpolator = AnimUtils.getFastOutSlowInInterpolator(
                    newHolder
                        .itemView.context
                )
                iconAlpha.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        dispatchChangeStarting(newHolder, false)
                        newHolder.itemView.setHasTransientState(true)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        newHolder.itemView.setHasTransientState(false)
                        dispatchChangeFinished(newHolder, false)
                    }
                })
                iconAlpha.start()
            } else if (preInfo.doHighlight) {
                val highlightColor =
                    ContextCompat.getColor(newHolder.itemView.context, R.color.accent)
                val fadeFromTo = ColorUtils.modifyAlpha(highlightColor, 0)

                ObjectAnimator.ofArgb(
                    newHolder.itemView,
                    ViewUtils.BACKGROUND_COLOR,
                    fadeFromTo,
                    highlightColor,
                    fadeFromTo
                ).apply {
                    duration = 1000L
                    interpolator = LinearInterpolator()
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator) {
                            dispatchChangeStarting(newHolder, false)
                            newHolder.itemView.setHasTransientState(true)
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            newHolder.itemView.background = null
                            newHolder.itemView.setHasTransientState(false)
                            dispatchChangeFinished(newHolder, false)
                        }
                    })
                }.start()
            }
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    companion object {

        const val FILTER_ENABLED = 1
        const val FILTER_DISABLED = 2
        const val HIGHLIGHT = 3
    }
}
