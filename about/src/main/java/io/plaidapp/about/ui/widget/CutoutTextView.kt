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

package io.plaidapp.about.ui.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.core.content.res.ResourcesCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import io.plaidapp.about.R
import io.plaidapp.core.util.ViewUtils
import io.plaidapp.core.R as coreR

/**
 * A view which punches out some text from an opaque color block, allowing you to see through it.
 */
class CutoutTextView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val maxTextSize: Float
    private val text: String

    private var cutout: Bitmap? = null
    private var foregroundColor = Color.MAGENTA
    private var textY = 0f
    private var textX = 0f

    init {

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.CutoutTextView, 0, 0)
        if (a.hasValue(R.styleable.CutoutTextView_android_fontFamily)) {
            try {
                val font = ResourcesCompat.getFont(
                    getContext(),
                    a.getResourceId(R.styleable.CutoutTextView_android_fontFamily, 0)
                )
                if (font != null) {
                    textPaint.typeface = font
                }
            } catch (nfe: Resources.NotFoundException) {
            }
        }
        if (a.hasValue(R.styleable.CutoutTextView_foregroundColor)) {
            foregroundColor = a.getColor(
                R.styleable.CutoutTextView_foregroundColor,
                foregroundColor
            )
        }
        text = if (a.hasValue(R.styleable.CutoutTextView_android_text)) {
            a.getString(R.styleable.CutoutTextView_android_text)
        } else {
            ""
        }
        maxTextSize = context.resources.getDimensionPixelSize(coreR.dimen.display_4_text_size).toFloat()
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateTextPosition()
        createBitmap()
    }

    private fun calculateTextPosition() {
        val targetWidth = width / PHI
        val textSize = ViewUtils.getSingleLineTextSize(
            text, textPaint,
            targetWidth, 0f, maxTextSize, 0.5f,
            resources.displayMetrics
        )
        textPaint.textSize = textSize

        // measuring text is fun :] see: https://chris.banes.me/2014/03/27/measuring-text/
        textX = (width - textPaint.measureText(text)) / 2
        val textBounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val textHeight = textBounds.height().toFloat()
        textY = (height + textHeight) / 2
    }

    private fun createBitmap() {
        cutout?.run {
            if (!isRecycled) {
                recycle()
            }
        }

        // this is the magic – Clear mode punches out the bitmap
        textPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

        cutout = createBitmap(width, height).applyCanvas {
            drawColor(foregroundColor)
            drawText(text, textX, textY, textPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(cutout, 0f, 0f, null)
    }

    override fun hasOverlappingRendering() = true

    companion object {

        private const val PHI = 1.6182f
    }
}
