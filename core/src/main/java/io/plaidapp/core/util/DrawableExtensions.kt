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

@file:JvmName("DrawableUtils")

package io.plaidapp.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.TransitionDrawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.gif.GifDrawable

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun drawableToBitmap(context: Context, @DrawableRes drawableId: Int) =
        ContextCompat.getDrawable(context, drawableId)?.toBitmap()

fun Drawable.isAnimated() = this is Animatable

val LayerDrawable.layers: List<Drawable>
    get() = (0 until numberOfLayers).map { getDrawable(it) }

/**
 * If the [Drawable] is a gif, it returns it as [GifDrawable]. Returns null otherwise.
 */
fun Drawable.asGif(): GifDrawable? {
    var gif: GifDrawable? = null
    if (this is GifDrawable) {
        return this
    } else if (this is TransitionDrawable) {
        // we fade in images on load which uses a TransitionDrawable; check its
        // layers
        val fadingIn = this
        for (i in 0 until this.numberOfLayers) {
            if (fadingIn.getDrawable(i) is GifDrawable) {
                gif = fadingIn.getDrawable(i) as GifDrawable
                break
            }
        }
    }
    return gif
}
