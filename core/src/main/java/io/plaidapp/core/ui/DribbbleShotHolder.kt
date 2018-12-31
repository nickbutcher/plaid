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
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.R
import io.plaidapp.core.ui.widget.BadgedFourThreeImageView
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ObservableColorMatrix

class DribbbleShotHolder constructor(
    itemView: View,
    private val initialGifBadgeColor: Int,
    private val onItemClicked: (image: View, position: Int) -> Unit,
    private val onItemTouched: (view: ImageView, event: MotionEvent) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    val image: BadgedFourThreeImageView = itemView as BadgedFourThreeImageView

    init {
        image.setBadgeColor(initialGifBadgeColor)
        image.setOnClickListener {
            onItemClicked(image, adapterPosition)
        }
        image.setOnTouchListener { _, event ->
            onItemTouched(image, event)
            return@setOnTouchListener false
        }
    }

    fun reset() {
        image.setBadgeColor(initialGifBadgeColor)
        image.drawBadge = false
        image.foreground = ContextCompat.getDrawable(image.context, R.drawable.mid_grey_ripple)
    }

    fun fade() {
        image.setHasTransientState(true)
        val cm = ObservableColorMatrix()
        val saturationAnimator = ObjectAnimator.ofFloat(
                cm, ObservableColorMatrix.SATURATION, 0f, 1f)
        saturationAnimator.apply {
            addUpdateListener { _ ->
                // just animating the color matrix does not invalidate the
                // drawable so need this update listener.  Also have to create a
                // new CMCF as the matrix is immutable :(
                image.colorFilter = ColorMatrixColorFilter(cm)
            }
            duration = 2000L
            interpolator = AnimUtils.getFastOutSlowInInterpolator(image.context)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    image.clearColorFilter()
                    image.setHasTransientState(false)
                }
            })
            start()
        }
    }

    fun prepareForFade(background: Drawable, drawBadge: Boolean, transitionName: String) {
        image.apply {
            this.background = background
            this.drawBadge = drawBadge
            this.transitionName = transitionName
        }
    }
}
