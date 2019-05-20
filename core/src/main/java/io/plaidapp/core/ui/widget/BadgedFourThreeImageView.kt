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

package io.plaidapp.core.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.SUBPIXEL_TEXT_FLAG
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuff.Mode.CLEAR
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import android.text.TextPaint
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import io.plaidapp.core.R

/**
 * A view group that draws a badge drawable on top of it's contents.
 */
class BadgedFourThreeImageView(
    context: Context,
    attrs: AttributeSet
) : FourThreeImageView(context, attrs) {

    var drawBadge = false
    private val badge: Drawable
    private var badgeBoundsSet = false
    private val badgeGravity: Int
    private val badgePadding: Int

    init {
        badge = GifBadge(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.BadgedImageView, 0, 0)
        badgeGravity = a.getInt(R.styleable.BadgedImageView_badgeGravity, Gravity.END or Gravity
                .BOTTOM)
        badgePadding = a.getDimensionPixelSize(R.styleable.BadgedImageView_badgePadding, 0)
        a.recycle()
    }

    fun setBadgeColor(@ColorInt color: Int) {
        badge.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    val badgeBounds: Rect
        get() {
            if (!badgeBoundsSet) {
                layoutBadge()
            }
            return badge.bounds
        }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (drawBadge) {
            if (!badgeBoundsSet) {
                layoutBadge()
            }
            badge.draw(canvas)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutBadge()
    }

    private fun layoutBadge() {
        val badgeBounds = badge.bounds
        Gravity.apply(badgeGravity,
                badge.intrinsicWidth,
                badge.intrinsicHeight,
                Rect(0, 0, width, height),
                badgePadding,
                badgePadding,
                badgeBounds)
        badge.bounds = badgeBounds
        badgeBoundsSet = true
    }

    /**
     * A drawable for indicating that an image is animated
     */
    private class GifBadge internal constructor(context: Context) : Drawable() {
        private val paint = Paint()

        init {
            if (bitmap == null) {
                val dm = context.resources.displayMetrics
                val density = dm.density
                val scaledDensity = dm.scaledDensity
                val textPaint = TextPaint(ANTI_ALIAS_FLAG or SUBPIXEL_TEXT_FLAG)
                textPaint.typeface = Typeface.create(TYPEFACE, TYPEFACE_STYLE)
                textPaint.textSize = TEXT_SIZE * scaledDensity

                val padding = PADDING * density
                val textBounds = Rect()
                textPaint.getTextBounds(GIF, 0, GIF.length, textBounds)
                val height = padding + textBounds.height() + padding
                val width = padding + textBounds.width() + padding
                bitmap = createBitmap(width.toInt(), height.toInt()).applyCanvas {
                    val backgroundPaint = Paint(ANTI_ALIAS_FLAG)
                    backgroundPaint.color = BACKGROUND_COLOR
                    val cornerRadius = CORNER_RADIUS * density
                    drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, backgroundPaint)
                    // punch out the word 'GIF', leaving transparency
                    textPaint.xfermode = PorterDuffXfermode(CLEAR)
                    drawText(GIF, padding, height - padding, textPaint)
                }
            }
        }

        override fun getIntrinsicWidth() = bitmap?.width ?: 0

        override fun getIntrinsicHeight() = bitmap?.height ?: 0

        override fun draw(canvas: Canvas) {
            canvas.drawBitmap(bitmap, bounds.left.toFloat(), bounds.top.toFloat(), paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
        }

        override fun setColorFilter(cf: ColorFilter?) {
            paint.colorFilter = cf
        }

        override fun getOpacity() = PixelFormat.TRANSLUCENT

        companion object {
            private const val GIF = "GIF"
            private const val TEXT_SIZE = 12    // sp
            private const val PADDING = 4       // dp
            private const val CORNER_RADIUS = 2 // dp
            private const val BACKGROUND_COLOR = Color.WHITE
            private const val TYPEFACE = "sans-serif-black"
            private const val TYPEFACE_STYLE = Typeface.NORMAL
            private var bitmap: Bitmap? = null
        }
    }
}
