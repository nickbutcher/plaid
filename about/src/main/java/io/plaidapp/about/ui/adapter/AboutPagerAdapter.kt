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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.plaidapp.about.databinding.AboutIconBinding
import io.plaidapp.about.databinding.AboutLibsBinding
import io.plaidapp.about.databinding.AboutPlaidBinding
import io.plaidapp.about.ui.model.AboutUiModel
import io.plaidapp.core.util.HtmlUtils
import java.security.InvalidParameterException

/**
 * Adapter creating and holding on to pages displayed within [io.plaidapp.about.ui.AboutActivity].
 */
internal class AboutPagerAdapter(private val uiModel: AboutUiModel) :
        RecyclerView.Adapter<AboutPagerAdapter.AboutViewHolder>() {

    private var aboutPlaid: View? = null
    private var aboutIcon: View? = null
    private var aboutLibs: View? = null

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> throw InvalidParameterException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
        return when (viewType) {
            0 -> AboutViewHolder(getAboutAppPage(parent))
            1 -> AboutViewHolder(getAboutIconPage(parent))
            2 -> AboutViewHolder(getAboutLibsPage(parent))
            else -> throw InvalidParameterException()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
        // do nothing
    }

    private fun getAboutAppPage(parent: ViewGroup): View {
        if (aboutPlaid == null) {
            AboutPlaidBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                HtmlUtils.setTextWithNiceLinks(aboutDescription, uiModel.appAboutText)
                aboutPlaid = root
            }
        }
        return aboutPlaid!!
    }

    private fun getAboutIconPage(parent: ViewGroup): View {
        if (aboutIcon == null) {
            AboutIconBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                HtmlUtils.setTextWithNiceLinks(iconDescription, uiModel.iconAboutText)
                aboutIcon = root
            }
        }
        return aboutIcon!!
    }

    private fun getAboutLibsPage(parent: ViewGroup): View {
        if (aboutLibs == null) {
            AboutLibsBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                libsList.adapter = LibraryAdapter(uiModel.librariesUiModel)
                aboutLibs = root
            }
        }
        return aboutLibs!!
    }

    class AboutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
