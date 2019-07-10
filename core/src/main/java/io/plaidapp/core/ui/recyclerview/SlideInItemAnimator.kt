/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.ui.recyclerview

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation.ALPHA
import androidx.dynamicanimation.animation.SpringAnimation.TRANSLATION_X
import androidx.dynamicanimation.animation.SpringAnimation.TRANSLATION_Y
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.core.util.listenForAllSpringsEnd
import io.plaidapp.core.util.spring

/**
 * A [RecyclerView.ItemAnimator] that fades & slides newly added items in from a given
 * direction.
 */
open class SlideInItemAnimator @JvmOverloads constructor(
    slideFromEdge: Int = Gravity.BOTTOM, // Default to sliding in upward
    layoutDirection: Int = -1
) : DefaultItemAnimator() {

    private val pendingAdds = mutableListOf<RecyclerView.ViewHolder>()
    private val slideFromEdge: Int = Gravity.getAbsoluteGravity(slideFromEdge, layoutDirection)

    init {
        addDuration = 160L
    }

    @SuppressLint("RtlHardcoded")
    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        holder.itemView.alpha = 0f
        when (slideFromEdge) {
            Gravity.LEFT -> holder.itemView.translationX = -holder.itemView.width / 3f
            Gravity.TOP -> holder.itemView.translationY = -holder.itemView.height / 3f
            Gravity.RIGHT -> holder.itemView.translationX = holder.itemView.width / 3f
            else // Gravity.BOTTOM
            -> holder.itemView.translationY = holder.itemView.height / 3f
        }
        pendingAdds.add(holder)
        return true
    }

    override fun runPendingAnimations() {
        super.runPendingAnimations()
        if (pendingAdds.isNotEmpty()) {
            for (i in pendingAdds.indices.reversed()) {
                val holder = pendingAdds[i]
                val springAlpha = holder.itemView.spring(ALPHA)
                val springTranslationX = holder.itemView.spring(TRANSLATION_X)
                val springTranslationY = holder.itemView.spring(TRANSLATION_Y)
                dispatchAddStarting(holder)
                springAlpha.animateToFinalPosition(1f)
                springTranslationX.animateToFinalPosition(0f)
                springTranslationY.animateToFinalPosition(0f)

                listenForAllSpringsEnd({ cancelled ->
                    if (cancelled) {
                        clearAnimatedValues(holder.itemView)
                    }
                    dispatchAddFinished(holder)
                    dispatchFinishedWhenDone()

                }, springAlpha, springTranslationX, springTranslationY)
                pendingAdds.removeAt(i)
            }
        }
    }

    override fun endAnimation(holder: RecyclerView.ViewHolder) {
        holder.itemView.spring(ALPHA).cancel()
        holder.itemView.spring(TRANSLATION_X).cancel()
        holder.itemView.spring(TRANSLATION_Y).cancel()
        if (pendingAdds.remove(holder)) {
            dispatchAddFinished(holder)
            clearAnimatedValues(holder.itemView)
        }
        super.endAnimation(holder)
    }

    override fun endAnimations() {
        for (i in pendingAdds.indices.reversed()) {
            val holder = pendingAdds[i]
            clearAnimatedValues(holder.itemView)
            dispatchAddFinished(holder)
            pendingAdds.removeAt(i)
        }
        super.endAnimations()
    }

    override fun isRunning(): Boolean {
        return pendingAdds.isNotEmpty() || super.isRunning()
    }

    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    private fun clearAnimatedValues(view: View) {
        view.alpha = 1f
        view.translationX = 0f
        view.translationY = 0f
    }
}
