/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.bumptech.glide.request.transition.Transition
import io.plaidapp.R
import io.plaidapp.ui.widget.BadgedFourThreeImageView
import io.plaidapp.util.ColorUtils
import io.plaidapp.util.isAnimated
import io.plaidapp.util.ViewUtils

/**
 * A Glide [ViewTarget] for [BadgedFourThreeImageView]s. It applies a badge for animated
 * images, can prevent GIFs from auto-playing & applies a palette generated ripple.
 */
class DribbbleTarget(
        view: BadgedFourThreeImageView,
        private val autoplayGifs: Boolean
) : DrawableImageViewTarget(view), Palette.PaletteAsyncListener {

    override fun onResourceReady(drawable: Drawable, transition: Transition<in Drawable>?) {
        super.onResourceReady(drawable, transition)
        val isAnimated = drawable.isAnimated()
        if (!autoplayGifs && isAnimated) {
            (drawable as GifDrawable).stop()
        }
        val bitmap = drawable.getBitmap() ?: return
        val badgedImageView = view as BadgedFourThreeImageView
        if (!isAnimated) {
            Palette.from(bitmap)
                    .clearFilters()
                    .generate(this)
        } else {
            Palette.from(bitmap).clearFilters().generate(this)
            // look at the corner to determine the gif badge color
            val cornerSize = (56 * view.context.resources.displayMetrics.scaledDensity).toInt()
            val corner = Bitmap.createBitmap(bitmap,
                    bitmap.width - cornerSize,
                    bitmap.height - cornerSize,
                    cornerSize, cornerSize)
            val isDark = ColorUtils.isDark(corner)
            corner.recycle()
            badgedImageView.setBadgeColor(ContextCompat.getColor(getView().context,
                    if (isDark) R.color.gif_badge_dark_image else R.color.gif_badge_light_image))
        }
    }

    override fun onStart() {
        if (autoplayGifs) {
            super.onStart()
        }
    }

    override fun onStop() {
        if (autoplayGifs) {
            super.onStop()
        }
    }

    override fun onGenerated(palette: Palette) {
        (view as BadgedFourThreeImageView).foreground = ViewUtils.createRipple(palette, 0.25f, 0.5f,
                ContextCompat.getColor(view.context, R.color.mid_grey), true)
    }

}
