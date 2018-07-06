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

import android.support.v4.view.PagerAdapter
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.security.InvalidParameterException
import io.plaidapp.about.R
import io.plaidapp.core.util.HtmlUtils

internal class AboutPagerAdapter(
    private val aboutViewModel: AboutViewModel,
    private val onClick: OnClick
) : PagerAdapter() {

    private lateinit var aboutPlaid: View
    private lateinit var aboutIcon: View
    private lateinit var aboutLibs: View
    private lateinit var layoutInflater: LayoutInflater

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        return getPage(position, collection).also {
            collection.addView(it)
        }
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount() = 3

    override fun isViewFromObject(view: View, obj: Any) = view === obj

    private fun getPage(position: Int, parent: ViewGroup): View {
        if (!::layoutInflater.isInitialized) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        return when (position) {
            0 -> {
                if (!::aboutPlaid.isInitialized) {
                    buildAppAboutPage(parent)
                }
                aboutPlaid
            }
            1 -> {
                if (!::aboutIcon.isInitialized) {
                    buildIconAboutPage(parent)
                }
                aboutIcon
            }
            2 -> {
                if (!::aboutLibs.isInitialized) {
                    buildLibsAboutPage(parent)
                }
                aboutLibs
            }
            else -> throw InvalidParameterException()
        }
    }

    private fun buildLibsAboutPage(parent: ViewGroup) {
        aboutLibs = layoutInflater.inflate(R.layout.about_libs, parent, false).also {
            it.findViewById<RecyclerView>(R.id.libs_list).apply {
                adapter = LibraryAdapter(aboutViewModel.libraries, onClick)
            }
        }
    }

    private fun buildIconAboutPage(parent: ViewGroup) {
        aboutIcon = layoutInflater.inflate(R.layout.about_icon, parent, false).also {
            it.findViewById<TextView>(R.id.icon_description).apply {
                HtmlUtils.setTextWithNiceLinks(this, aboutViewModel.iconAboutText)
            }
        }
    }

    private fun buildAppAboutPage(parent: ViewGroup) {
        aboutPlaid = layoutInflater.inflate(R.layout.about_plaid, parent, false).also {
            it.findViewById<TextView>(R.id.about_description).apply {
                HtmlUtils.setTextWithNiceLinks(this, aboutViewModel.appAboutText)
            }
        }
    }
}
