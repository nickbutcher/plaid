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

    private var aboutPlaidBinding: AboutPlaidBinding? = null
    private var aboutIconBinding: AboutIconBinding? = null
    private var aboutLibsBinding: AboutLibsBinding? = null

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> 0
            1 -> 1
            2 -> 2
            else -> throw InvalidParameterException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
        return AboutViewHolder(
            when (viewType) {
                0 -> getAboutAppPage(parent)
                1 -> getAboutIconPage(parent)
                2 -> getAboutLibsPage(parent)
                else -> throw InvalidParameterException()
            }
        )
    }

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
        // do nothing
    }

    private fun getAboutAppPage(parent: ViewGroup): View {
        val binding = aboutPlaidBinding ?: AboutPlaidBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            HtmlUtils.setTextWithNiceLinks(aboutDescription, uiModel.appAboutText)
            aboutPlaidBinding = this
        }
        return binding.root
    }

    private fun getAboutIconPage(parent: ViewGroup): View {
        val binding = aboutIconBinding ?: AboutIconBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            HtmlUtils.setTextWithNiceLinks(iconDescription, uiModel.iconAboutText)
            aboutIconBinding = this
        }
        return binding.root
    }

    private fun getAboutLibsPage(parent: ViewGroup): View {
        val binding = aboutLibsBinding ?: AboutLibsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).apply {
            libsList.adapter = LibraryAdapter(uiModel.librariesUiModel)
            aboutLibsBinding = this
        }
        return binding.root
    }

    class AboutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
