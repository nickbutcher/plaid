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

package io.plaidapp.core.producthunt.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import io.plaidapp.core.R
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.ui.recyclerview.Divided

/**
 * ViewHolder for a Product Hunt Post
 */
class ProductHuntPostHolder(
    itemView: View,
    private val commentsClicked: (post: Post) -> Unit,
    private val viewClicked: (post: Post) -> Unit
) : RecyclerView.ViewHolder(itemView), Divided {

    private var post: Post? = null
    private var title: TextView = itemView.findViewById(R.id.hunt_title)
    private var tagline: TextView = itemView.findViewById(R.id.tagline)
    private var comments: TextView = itemView.findViewById(R.id.story_comments)

    init {
        comments.setOnClickListener { post?.let { commentsClicked(it) } }
        itemView.setOnClickListener { post?.let { viewClicked(it) } }
    }

    fun bind(item: Post) {
        post = item
        title.text = item.name
        tagline.text = item.tagline
        comments.text = item.commentsCount.toString()
    }
}
