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

package io.plaidapp.core.util.glide

import `in`.uncod.android.bypass.style.ImageLoadingSpan
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.transition.TransitionManager
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.lang.ref.WeakReference

/**
 * A target that puts a downloaded image into an ImageSpan in the provided TextView.  It uses a
 * [ImageLoadingSpan] to mark the area to be replaced by the image.
 */
class ImageSpanTarget(
    textView: TextView,
    private val loadingSpan: ImageLoadingSpan
) : SimpleTarget<Bitmap>() {

    private val textView: WeakReference<TextView> = WeakReference(textView)

    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
        val tv = textView.get()
        if (tv != null) {
            val bitmapDrawable = BitmapDrawable(tv.resources, bitmap)
            // image span doesn't handle scaling so we manually set bounds
            if (bitmap.width > tv.width) {
                val aspectRatio = bitmap.height.toFloat() / bitmap.width.toFloat()
                bitmapDrawable.setBounds(0, 0, tv.width, (aspectRatio * tv.width).toInt())
            } else {
                bitmapDrawable.setBounds(0, 0, bitmap.width, bitmap.height)
            }
            val span = ImageSpan(bitmapDrawable)
            // add the image span and remove our marker
            with(SpannableStringBuilder(tv.text)) {
                val start = getSpanStart(loadingSpan)
                val end = getSpanEnd(loadingSpan)
                if (start >= 0 && end >= 0) {
                    setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                removeSpan(loadingSpan)
                // animate the change
                TransitionManager.beginDelayedTransition(tv.parent as ViewGroup)
                tv.text = this
            }
        }
    }
}
