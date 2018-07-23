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

package io.plaidapp.about.ui.adapter

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.plaidapp.about.R
import io.plaidapp.about.domain.model.Library
import io.plaidapp.core.util.glide.GlideApp
import io.plaidapp.R as appR

internal typealias OnClick = (library: Library) -> Unit

internal class LibraryHolder(
    itemView: View,
    private val onClick: OnClick
) : RecyclerView.ViewHolder(itemView) {

    private var library: Library? = null

    private var image: ImageView = itemView.findViewById(R.id.library_image)
    private var name: TextView = itemView.findViewById(R.id.library_name)
    private var description: TextView = itemView.findViewById(R.id.library_description)
    private var link: Button = itemView.findViewById(R.id.library_link)

    init {
        View.OnClickListener { library?.let { onClick(it) } }.apply {
            itemView.setOnClickListener(this)
            link.setOnClickListener(this)
        }
    }

    @SuppressLint("CheckResult")
    fun bind(lib: Library) {
        library = lib
        name.text = lib.name
        description.text = lib.description
        val request = GlideApp.with(image.context)
                .load(lib.imageUrl)
                .transition(withCrossFade())
                .placeholder(appR.drawable.avatar_placeholder)
        if (lib.circleCrop) {
            request.circleCrop()
        }
        request.into(image)
    }
}
