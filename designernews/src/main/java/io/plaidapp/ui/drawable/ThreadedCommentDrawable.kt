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

package io.plaidapp.ui.drawable

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.annotation.Px

@ColorInt private const val THREAD_COLOR = 0xffff00ff.toInt()

/**
 * A drawable showing the depth of a threaded conversation
 */
class ThreadedCommentDrawable(
    @Px private val threadWidth: Int,
    @Px private val gap: Int
) : Drawable() {

    private val halfThreadWidth: Int = threadWidth / 2
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = threadWidth.toFloat()
        color = THREAD_COLOR
    }
    private var threads: Int = 0

    constructor(threadWidth: Int, gap: Int, depth: Int) : this(threadWidth, gap) {
        setDepth(depth)
    }

    fun setDepth(depth: Int) {
        this.threads = depth + 1
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        for (thread in 0 until threads) {
            val left = halfThreadWidth + thread * (threadWidth + gap)
            canvas.drawLine(left.toFloat(), 0f, left.toFloat(), bounds.bottom.toFloat(), paint)
        }
    }

    override fun getIntrinsicWidth(): Int {
        return threads * threadWidth + (threads - 1) * gap
    }

    override fun setAlpha(i: Int) {
        paint.alpha = i
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun setTintList(tint: ColorStateList?) {
        if (tint != null) {
            setColorFilter(tint.defaultColor, SRC_IN)
        } else {
            clearColorFilter()
        }
    }

    override fun setTint(tintColor: Int) {
        setColorFilter(tintColor, SRC_IN)
    }

    override fun getOpacity(): Int {
        return if (paint.alpha == 255) PixelFormat.OPAQUE else PixelFormat.TRANSLUCENT
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThreadedCommentDrawable

        if (threads != other.threads) return false

        return true
    }

    override fun hashCode(): Int {
        return threads
    }
}
