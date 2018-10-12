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

package io.plaidapp.designernews.ui.story

import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.ui.recyclerview.SlideInItemAnimator

/**
 * Slide in animator for Designer News comments
 */
internal class CommentAnimator(addRemoveDuration: Long) : SlideInItemAnimator() {

    init {
        addDuration = addRemoveDuration
        removeDuration = addRemoveDuration
    }

    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return true
    }

    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: List<Any>
    ): RecyclerView.ItemAnimator.ItemHolderInfo {
        val info = super.recordPreLayoutInformation(
                state, viewHolder, changeFlags, payloads) as CommentItemHolderInfo
        info.doExpand = payloads.contains(EXPAND_COMMENT)
        info.doCollapse = payloads.contains(COLLAPSE_COMMENT)
        return info
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: RecyclerView.ItemAnimator.ItemHolderInfo,
        postInfo: RecyclerView.ItemAnimator.ItemHolderInfo
    ): Boolean {
        if (newHolder is CommentViewHolder && preInfo is CommentItemHolderInfo) {
            if (preInfo.doExpand) {
                newHolder.expand(this)
            } else if (preInfo.doCollapse) {
                newHolder.collapse(this)
            }
        }
        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    override fun obtainHolderInfo(): RecyclerView.ItemAnimator.ItemHolderInfo {
        return CommentItemHolderInfo()
    }

    private class CommentItemHolderInfo : RecyclerView.ItemAnimator.ItemHolderInfo() {
        internal var doExpand: Boolean = false
        internal var doCollapse: Boolean = false
    }

    companion object {
        const val EXPAND_COMMENT = 1
        const val COLLAPSE_COMMENT = 2
    }
}
