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

package io.plaidapp.dribbble.ui.shot

import android.databinding.BindingAdapter
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.PluralsRes
import android.support.v4.content.ContextCompat
import android.support.v7.content.res.AppCompatResources
import android.text.format.DateUtils
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Priority.IMMEDIATE
import com.bumptech.glide.load.engine.DiskCacheStrategy.DATA
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.util.HtmlUtils
import io.plaidapp.core.util.glide.GlideApp
import io.plaidapp.dribbble.R
import java.text.NumberFormat
import java.util.Date

@BindingAdapter("numberFormattedPlural", "pluralQuantity", requireAll = true)
fun bindNumberFormattedPlural(
    textView: TextView,
    @PluralsRes plural: Int,
    pluralQuantity: Int
) {
    @Suppress("DEPRECATION")
    val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        NumberFormat.getInstance(textView.resources.configuration.locales[0])
    } else {
        NumberFormat.getInstance(textView.resources.configuration.locale)
    }
    textView.text = textView.resources.getQuantityString(
        plural,
        pluralQuantity,
        formatter.format(pluralQuantity)
    )
}

@BindingAdapter("relativeTime")
fun bindRelativeTime(
    textView: TextView,
    date: Date?
) {
    date?.let {
        textView.text = DateUtils.getRelativeTimeSpanString(
            it.time,
            System.currentTimeMillis(),
            DateUtils.SECOND_IN_MILLIS
        ).toString().toLowerCase()
    }
}

@BindingAdapter("animatedOnClick")
fun bindAnimatedOnClick(
    textView: TextView,
    onClickAction: () -> Unit
) = setAnimatedOnClick(textView, onClickAction)

@BindingAdapter("animatedOnClick")
fun bindAnimatedOnClick(
    textView: TextView,
    enabled: Boolean
) = setAnimatedOnClick(textView, enabled = enabled)

private fun setAnimatedOnClick(
    textView: TextView,
    onClickAction: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    textView.setOnClickListener {
        // If the top compound drawable is an AVD then start it when clicked
        (textView.compoundDrawables[1] as? AnimatedVectorDrawable)?.start()
        if (enabled) {
            onClickAction?.invoke()
        }
    }
}

@BindingAdapter("shot", "listener", requireAll = true)
fun bindShot(
    imageView: ImageView,
    shot: Shot,
    listener: RequestListener<Drawable>
) {
    val (width, height) = shot.images.bestSize()
    GlideApp.with(imageView.context)
        .load(shot.images.best())
        .listener(listener)
        .diskCacheStrategy(DATA)
        .priority(IMMEDIATE)
        .override(width, height)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(imageView)
}

@BindingAdapter("imageUrl", "placeholder", "circleCrop", "crossFade", requireAll = false)
fun bindImage(
    imageView: ImageView,
    imageUrl: String,
    placeholder: Int? = null,
    circleCrop: Boolean? = false,
    crossFade: Boolean? = false
) {
    var request = GlideApp.with(imageView.context).load(imageUrl)
    if (placeholder != null) {
        request = request.placeholder(placeholder)
    }
    if (circleCrop == true) {
        request = request.circleCrop()
    }
    if (crossFade == true) {
        request = request.transition(DrawableTransitionOptions.withCrossFade())
    }
    request.into(imageView)
}

@BindingAdapter("htmlText")
fun bindHtmlText(
    textView: TextView,
    text: String
) {
    val descText = HtmlUtils.parseHtml(
        text,
        AppCompatResources.getColorStateList(textView.context, R.color.dribbble_links),
        ContextCompat.getColor(textView.context, io.plaidapp.R.color.dribbble_link_highlight)
    )
    HtmlUtils.setTextWithNiceLinks(textView, descText)
}
