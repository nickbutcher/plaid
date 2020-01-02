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

package io.plaidapp.ui.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.plaidapp.R
import io.plaidapp.core.ui.filter.FilterViewHolder
import io.plaidapp.core.ui.recyclerview.FilterSwipeDismissListener
import io.plaidapp.util.setTranslation

/**
 * Callback for swiping a custom search filter to delete it.
 */
class FilterTouchHelperCallback(
    private val listener: FilterSwipeDismissListener,
    context: Context
) : ItemTouchHelper.SimpleCallback(0, START) {

    private val backgroundColor: Int
    private val shadowColor: Int
    private val deleteColor: Int
    private val iconPadding: Int
    private val topShadowHeight: Float
    private val bottomShadowHeight: Float
    private val sideShadowWidth: Float

    // lazily initialized later
    private var initialized = false
    private var iconColorFilter: Int
    private var deleteIcon: Drawable? = null
    private var circlePaint: Paint? = null
    private var leftShadowPaint: Paint? = null
    private var topShadowPaint: Paint? = null
    private var bottomShadowPaint: Paint? = null

    init {
        val res = context.resources
        backgroundColor = ContextCompat.getColor(context, R.color.background_super_dark)
        shadowColor = ContextCompat.getColor(context, R.color.shadow)
        deleteColor = ContextCompat.getColor(context, R.color.delete)
        iconColorFilter = deleteColor
        iconPadding = res.getDimensionPixelSize(R.dimen.padding_normal)
        // faking elevation light-source; so use different shadow sizes
        topShadowHeight = res.getDimension(R.dimen.spacing_micro)
        bottomShadowHeight = topShadowHeight / 2f
        sideShadowWidth = topShadowHeight * 3f / 4f
    }

    // don't support re-ordering
    override fun onMove(rv: RecyclerView, source: ViewHolder, target: ViewHolder) = false

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        listener.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getSwipeDirs(rv: RecyclerView, viewHolder: ViewHolder): Int {
        // can only swipe-dismiss certain sources
        val swipeDir = if ((viewHolder as FilterViewHolder).isSwipeable) START else 0
        return makeMovementFlags(0, swipeDir)
    }

    // make deleting a deliberate gesture
    override fun getSwipeEscapeVelocity(defaultValue: Float) = defaultValue * 5f

    override fun isLongPressDragEnabled() = false

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // bail fast if there isn't a swipe
        if (dX == 0f) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        val left = viewHolder.itemView.left.toFloat()
        val top = viewHolder.itemView.top.toFloat()
        val right = viewHolder.itemView.right.toFloat()
        val bottom = viewHolder.itemView.bottom.toFloat()
        val width = right - left
        val height = bottom - top
        val saveCount = c.save()

        // clip to the 'revealed' area
        c.clipRect(right + dX, top, right, bottom)
        c.drawColor(backgroundColor)

        // lazy initialize some vars
        initialize(recyclerView.context)

        // variables dependent upon gesture progress
        val progress = -dX / width
        val swipeThreshold = getSwipeThreshold(viewHolder)
        val thirdThreshold = swipeThreshold / 3f
        val iconPopThreshold = swipeThreshold + 0.125f
        val iconPopFinishedThreshold = iconPopThreshold + 0.125f
        var opacity = 1f
        var iconScale = 1f
        var circleRadius = 0f
        var iconColor = deleteColor
        when (progress) {
            in 0f..thirdThreshold -> {
                // fade in
                opacity = progress / thirdThreshold
            }
            in thirdThreshold..swipeThreshold -> {
                // scale icon down to 0.9
                iconScale = 1f -
                        (((progress - thirdThreshold) / (swipeThreshold - thirdThreshold)) * 0.1f)
            }
            else -> {
                // draw circle and switch icon color
                circleRadius = (progress - swipeThreshold) * width * CIRCLE_ACCELERATION
                iconColor = Color.WHITE
                // scale icon up to 1.2 then back down to 1
                iconScale = when (progress) {
                    in swipeThreshold..iconPopThreshold -> {
                        0.9f + ((progress - swipeThreshold) / (iconPopThreshold - swipeThreshold)) *
                                0.3f
                    }
                    in iconPopThreshold..iconPopFinishedThreshold -> {
                        1.2f - (((progress - iconPopThreshold) /
                                (iconPopFinishedThreshold - iconPopThreshold)) * 0.2f)
                    }
                    else -> 1f
                }
            }
        }

        deleteIcon?.let {
            val cx = right - iconPadding - it.intrinsicWidth / 2f
            val cy = top + height / 2f
            val halfIconSize = it.intrinsicWidth * iconScale / 2f
            it.setBounds((cx - halfIconSize).toInt(), (cy - halfIconSize).toInt(),
                    (cx + halfIconSize).toInt(), (cy + halfIconSize).toInt())
            it.alpha = (opacity * 255f).toInt()
            if (iconColor != iconColorFilter) {
                it.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
                iconColorFilter = iconColor
            }
            if (circleRadius > 0f) {
                circlePaint?.let { paint ->
                    c.drawCircle(cx, cy, circleRadius, paint)
                }
            }
            it.draw(c)
        }

        // draw shadows to fake elevation of surrounding views
        topShadowPaint?.let {
            it.shader?.setTranslation(y = top)
            c.drawRect(left, top, right, top + topShadowHeight, it)
        }
        bottomShadowPaint?.let {
            it.shader?.setTranslation(y = bottom - bottomShadowHeight)
            c.drawRect(left, bottom - bottomShadowHeight, right, bottom, it)
        }
        leftShadowPaint?.let {
            val shadowLeft = right + dX
            it.shader?.setTranslation(x = shadowLeft)
            c.drawRect(shadowLeft, top, shadowLeft + sideShadowWidth, bottom, it)
        }

        c.restoreToCount(saveCount)
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun initialize(context: Context) {
        if (!initialized) {
            deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
            topShadowPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, topShadowHeight, shadowColor, 0, CLAMP)
            }
            bottomShadowPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, bottomShadowHeight, 0, shadowColor, CLAMP)
            }
            leftShadowPaint = Paint().apply {
                shader = LinearGradient(0f, 0f, sideShadowWidth, 0f, shadowColor, 0, CLAMP)
            }
            circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = deleteColor
            }
            initialized = true
        }
    }

    companion object {
        // expand the circle rapidly once it shows, don't track swipe 1:1
        private const val CIRCLE_ACCELERATION = 3f
    }
}
