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

package io.plaidapp.core.ui.filter

import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_DISABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.FILTER_ENABLED
import io.plaidapp.core.ui.filter.FilterHolderInfo.Companion.HIGHLIGHT

class FilterAnimator : DefaultItemAnimator() {

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun obtainHolderInfo(): ItemHolderInfo {
        return FilterHolderInfo()
    }

    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): ItemHolderInfo {
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
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        if (newHolder is FilterViewHolder && preInfo is FilterHolderInfo) {
            if (preInfo.doEnable || preInfo.doDisable) {
                animateEnableDisable(newHolder, preInfo)
            } else if (preInfo.doHighlight) {
                animateHighlight(newHolder)
            }
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    private fun animateHighlight(newHolder: FilterViewHolder) {
        val animator = newHolder.createHighlightAnimator()
        animator.apply {
            doOnStart { dispatchChangeStarting(newHolder, false) }
            doOnEnd { dispatchChangeFinished(newHolder, false) }
        }
        animator.start()
    }

    private fun animateEnableDisable(newHolder: FilterViewHolder, preInfo: FilterHolderInfo) {
        val animator = newHolder.createEnableDisableAnimator(preInfo)
        animator.apply {
            doOnStart { dispatchChangeStarting(newHolder, false) }
            doOnEnd { dispatchChangeFinished(newHolder, false) }
        }
        animator.start()
    }
}
