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

package io.plaidapp.ui.about

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade

import io.plaidapp.about.R
import io.plaidapp.base.util.glide.GlideApp

internal class LibraryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var image: ImageView = itemView.findViewById(R.id.library_image)
    var name: TextView = itemView.findViewById(R.id.library_name)
    var description: TextView = itemView.findViewById(R.id.library_description)
    var link: Button = itemView.findViewById(R.id.library_link)

    fun bind(lib: Library) {
        name.text = lib.name
        description.text = lib.description
        val request = GlideApp.with(image.context)
                .load(lib.imageUrl)
                .transition(withCrossFade())
                .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
        if (lib.circleCrop) {
            request.circleCrop()
        }
        request.into(image)
    }
}
