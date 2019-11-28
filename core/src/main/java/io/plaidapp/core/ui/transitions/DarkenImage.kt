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

package io.plaidapp.core.ui.transitions

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.transition.Transition
import android.transition.TransitionValues
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import io.plaidapp.core.R
import io.plaidapp.core.util.ColorUtils

private const val ALPHA_SCALE = 1.0f

/**
 * A transition that animates the RGB scale of an [ImageView]s `drawable` when in dark mode.
 */
class DarkenImage(context: Context, attrs: AttributeSet) : Transition(context, attrs) {

    private val isDarkTheme = ColorUtils.isDarkTheme(context)
    private val initialRgbScale: Float
    private val finalRgbScale: Float

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.DarkenImage)
        initialRgbScale = ta.getFloat(R.styleable.DarkenImage_initialRgbScale, 1.0f)
        finalRgbScale = ta.getFloat(R.styleable.DarkenImage_finalRgbScale, 1.0f)
        ta.recycle()
    }

    override fun captureStartValues(transitionValues: TransitionValues?) { }

    override fun captureEndValues(transitionValues: TransitionValues?) { }

    override fun createAnimator(
        sceneRoot: ViewGroup?,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (!isDarkTheme) return null
        if (initialRgbScale == finalRgbScale) return null
        val iv = endValues?.view as? ImageView ?: return null
        val drawable = iv.drawable ?: return null
        return ValueAnimator.ofFloat(initialRgbScale, finalRgbScale).apply {
            addUpdateListener { listener ->
                val cm = ColorMatrix()
                val rgbScale = listener.animatedValue as Float
                cm.setScale(rgbScale, rgbScale, rgbScale, ALPHA_SCALE)
                drawable.colorFilter = ColorMatrixColorFilter(cm)
            }
        }
    }
}
