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

package io.plaidapp.core.util

import androidx.databinding.BindingAdapter
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import io.plaidapp.core.util.glide.GlideApp

@BindingAdapter("goneUnless")
fun bindGoneUnless(view: View, gone: Boolean) {
    view.visibility = if (gone) {
        GONE
    } else {
        VISIBLE
    }
}

@BindingAdapter("visibleUnless")
fun bindVisibleUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) {
        VISIBLE
    } else {
        GONE
    }
}

@BindingAdapter("imageUrl", "placeholder", "circleCrop", "crossFade", requireAll = false)
fun bindImage(
    imageView: ImageView,
    imageUrl: String?,
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
