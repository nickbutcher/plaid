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

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.R
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.ViewUtils

private const val FILTER_ICON_ENABLED_ALPHA = 179 // 70%
private const val FILTER_ICON_DISABLED_ALPHA = 51 // 20%

/**
 * ViewHolder for filters.
 */
class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val filterName: TextView = itemView.findViewById(R.id.filter_name)
    private val filterIcon: ImageView = itemView.findViewById(R.id.filter_icon)
    var isSwipeable: Boolean = false
        private set(value) {
            field = value
        }

    fun enableFilter(enable: Boolean) {
        filterName.isEnabled = enable
    }

    fun bind(filter: SourceUiModel) {
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

    fun createEnableDisableAnimator(preInfo: FilterHolderInfo): Animator {
        val iconAlpha = ObjectAnimator.ofInt(
                filterIcon,
                ViewUtils.IMAGE_ALPHA,
                if (preInfo.doEnable) {
                    FILTER_ICON_ENABLED_ALPHA
                } else {
                    FILTER_ICON_DISABLED_ALPHA
                }
        )
        iconAlpha.apply {
            duration = 300L
            interpolator = AnimUtils.getFastOutSlowInInterpolator(itemView.context)
            doOnStart { itemView.setHasTransientState(true) }
            doOnEnd { itemView.setHasTransientState(false) }
        }

        return iconAlpha
    }

    fun createHighlightAnimator(): Animator {
        val highlightColor = ColorUtils.getThemeColor(itemView.context, R.attr.colorPrimary)
        val fadeFromTo = ColorUtils.modifyAlpha(highlightColor, 0)

        return ObjectAnimator.ofArgb(
                itemView,
                ViewUtils.BACKGROUND_COLOR,
                fadeFromTo,
                highlightColor,
                fadeFromTo
        ).apply {
            duration = 1000L
            interpolator = LinearInterpolator()

            doOnStart { itemView.setHasTransientState(true) }
            doOnEnd {
                itemView.background = null
                itemView.setHasTransientState(false)
            }
        }
    }
}
