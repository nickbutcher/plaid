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

@file:JvmName("GlideUtils")
package io.plaidapp.core.util.glide

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import com.bumptech.glide.load.resource.gif.GifDrawable
import io.plaidapp.core.util.layers

fun Drawable.getBitmap(): Bitmap? {
    if (this is TransitionDrawable) {
        layers.forEach {
            val bmp = it.getBitmap()
            if (bmp != null) return bmp
        }
    }
    if (this is BitmapDrawable) {
        return bitmap
    } else if (this is GifDrawable) {
        return firstFrame
    }
    return null
}
