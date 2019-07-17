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
import android.app.Activity
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.util.ViewPreloadSizeProvider
import io.plaidapp.core.R
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.ui.DribbbleShotHolder.Companion.getShotLoadingPlaceholders
import io.plaidapp.core.ui.recyclerview.FeedViewHolder
import io.plaidapp.core.ui.widget.BadgedFourThreeImageView
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ObservableColorMatrix
import io.plaidapp.core.util.asGif
import io.plaidapp.core.util.glide.DribbbleTarget
import io.plaidapp.core.util.glide.GlideApp

private const val NIGHT_MODE_RGB_SCALE = 0.85f
private const val ALPHA_SCALE = 1.0f

/**
 * [FeedViewHolder] for Dribbble [Shot] items.
 * Make sure to call [getShotLoadingPlaceholders] within the adapter to provide [ShotStyle].
 */
class DribbbleShotHolder constructor(
    itemView: View,
    private val isNightMode: Boolean,
    private val shotStyle: ShotStyle,
    private val onItemClicked: (image: View, position: Int) -> Unit
) : FeedViewHolder<Shot>(itemView) {
    val image: BadgedFourThreeImageView = itemView as BadgedFourThreeImageView

    private val shotPreloadSizeProvider = ViewPreloadSizeProvider<Shot>()

    init {
        image.setBadgeColor(shotStyle.initialGifBadgeColor)
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

    override fun bind(item: Shot) {
        val imageSize = item.images.bestSize()
        val requestListener = object : RequestListener<Drawable> {
            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                if (!item.hasFadedIn) {
                    fade()
                    item.hasFadedIn = true
                }
                return false
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ) = false
        }
        GlideApp.with(image)
            .load(item.images.best())
            .listener(requestListener)
            .placeholder(
                shotStyle.shotLoadingPlaceholders[adapterPosition %
                    shotStyle.shotLoadingPlaceholders.size]
            )
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(imageSize.width, imageSize.height)
            .into(DribbbleTarget(image, false))
        // need both placeholder & background to prevent seeing through shot as it fades in
        shotStyle.shotLoadingPlaceholders[adapterPosition % shotStyle.shotLoadingPlaceholders.size]
            ?.apply {
                prepareForFade(
                    this,
                    item.animated,
                    // need a unique transition name per shot, let's use its url
                    item.htmlUrl
                )
            }
        shotPreloadSizeProvider.setView(image)
    }

    fun reset() {
        image.setBadgeColor(shotStyle.initialGifBadgeColor)
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

    companion object {

        fun getShotLoadingPlaceholders(host: Activity): ShotStyle {
            val shotLoadingPlaceholders: Array<ColorDrawable?>

            // get the dribbble shot placeholder colors & badge color from the theme
            val a = host.obtainStyledAttributes(R.styleable.DribbbleFeed)
            val loadingColorArrayId =
                a.getResourceId(R.styleable.DribbbleFeed_shotLoadingPlaceholderColors, 0)
            if (loadingColorArrayId != 0) {
                val placeholderColors = host.resources.getIntArray(loadingColorArrayId)
                shotLoadingPlaceholders = arrayOfNulls(placeholderColors.size)
                placeholderColors.indices.forEach {
                    shotLoadingPlaceholders[it] = ColorDrawable(placeholderColors[it])
                }
            } else {
                shotLoadingPlaceholders = arrayOf(ColorDrawable(Color.DKGRAY))
            }
            val initialGifBadgeColorId =
                a.getResourceId(R.styleable.DribbbleFeed_initialBadgeColor, 0)

            val style = ShotStyle(
                shotLoadingPlaceholders,
                if (initialGifBadgeColorId != 0) {
                    ContextCompat.getColor(host, initialGifBadgeColorId)
                } else {
                    0x40ffffff
                }
            )
            a.recycle()

            return style
        }
    }
}

data class ShotStyle(
    val shotLoadingPlaceholders: Array<ColorDrawable?>,
    @ColorInt val initialGifBadgeColor: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShotStyle

        if (!shotLoadingPlaceholders.contentEquals(other.shotLoadingPlaceholders)) return false
        if (initialGifBadgeColor != other.initialGifBadgeColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shotLoadingPlaceholders.contentHashCode()
        result = 31 * result + initialGifBadgeColor
        return result
    }
}
