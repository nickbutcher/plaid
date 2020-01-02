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

package io.plaidapp.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.R
import io.plaidapp.core.ui.widget.BadgedFourThreeImageView
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ObservableColorMatrix
import io.plaidapp.core.util.asGif

private const val NIGHT_MODE_RGB_SCALE = 0.85f
private const val ALPHA_SCALE = 1.0f

class DribbbleShotHolder constructor(
    itemView: View,
    private val initialGifBadgeColor: Int,
    private val isNightMode: Boolean,
    private val onItemClicked: (image: View, position: Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    val image: BadgedFourThreeImageView = itemView as BadgedFourThreeImageView

    init {
        image.setBadgeColor(initialGifBadgeColor)
        image.setOnClickListener {
            onItemClicked(image, adapterPosition)
        }
        image.setOnTouchListener { _, event ->
            // play animated GIFs whilst touched
            // check if it's an event we care about, else bail fast
            val action = event.action
            if (!(action == MotionEvent.ACTION_DOWN ||
                    action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_CANCEL)
            ) {
                return@setOnTouchListener false
            }

            // get the image and check if it's an animated GIF
            val drawable = image.drawable ?: return@setOnTouchListener false
            val gif = drawable.asGif() ?: return@setOnTouchListener false
            // GIF found, start/stop it on press/lift
            when (action) {
                MotionEvent.ACTION_DOWN -> gif.start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> gif.stop()
            }
            return@setOnTouchListener false
        }
        darkenImage()
    }

    fun reset() {
        image.setBadgeColor(initialGifBadgeColor)
        image.drawBadge = false
        image.foreground = ContextCompat.getDrawable(image.context, R.drawable.mid_grey_ripple)
    }

    fun fade() {
        image.setHasTransientState(true)
        val cm = ObservableColorMatrix()
        ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f).apply {
            addUpdateListener {
                // Setting the saturation overwrites any darkening so need to reapply.
                // Just animating the color matrix does not invalidate the
                // drawable so need this update listener.  Also have to create a
                // new CMCF as the matrix is immutable :(
                darkenImage(cm)
            }
            duration = 2000L
            interpolator = AnimUtils.getFastOutSlowInInterpolator(image.context)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
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

    private fun darkenImage(colorMatrix: ColorMatrix = ColorMatrix()) {
        if (isNightMode) {
            colorMatrix.setScale(
                NIGHT_MODE_RGB_SCALE,
                NIGHT_MODE_RGB_SCALE,
                NIGHT_MODE_RGB_SCALE,
                ALPHA_SCALE
            )
        }
        image.colorFilter = ColorMatrixColorFilter(colorMatrix)
    }
}
