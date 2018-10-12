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

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.plaidapp.about.R
import io.plaidapp.about.ui.model.LibrariesUiModel
import java.security.InvalidParameterException

/**
 * Adapter that holds libraries.
 */
internal class LibraryAdapter(
    private val uiModel: LibrariesUiModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_INTRO -> LibraryIntroHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.about_lib_intro, parent, false)
            )
            VIEW_TYPE_LIBRARY -> createLibraryHolder(parent)
            else -> throw InvalidParameterException()
        }
    }

    private fun createLibraryHolder(parent: ViewGroup): LibraryHolder {
        return LibraryHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.library, parent, false),
            uiModel.onClick
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_LIBRARY) {
            (holder as LibraryHolder).bind(uiModel.libraries[position - 1]) // adjust for intro
        }
    }

    override fun getItemViewType(position: Int) =
            if (position == 0) VIEW_TYPE_INTRO else VIEW_TYPE_LIBRARY

    override fun getItemCount() = uiModel.libraries.size + 1 // + 1 for the static intro view

    companion object {
        private const val VIEW_TYPE_INTRO = 0
        private const val VIEW_TYPE_LIBRARY = 1
    }
}
