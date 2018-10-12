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

import androidx.viewpager.widget.PagerAdapter
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.plaidapp.about.R
import io.plaidapp.core.util.HtmlUtils
import io.plaidapp.about.ui.model.AboutUiModel
import io.plaidapp.core.util.inflateView
import java.security.InvalidParameterException

/**
 * Adapter creating and holding on to pages displayed within [io.plaidapp.about.ui.AboutActivity].
 */
internal class AboutPagerAdapter(private val uiModel: AboutUiModel) : PagerAdapter() {

    private var aboutPlaid: View? = null
    private var aboutIcon: View? = null
    private var aboutLibs: View? = null

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
        return when (position) {
            0 -> getAboutAppPage(parent)
            1 -> getAboutIconPage(parent)
            2 -> getAboutLibsPage(parent)
            else -> throw InvalidParameterException()
        }
    }

    private fun getAboutIconPage(parent: ViewGroup): View {
        return aboutIcon ?: parent.inflateView(R.layout.about_icon).apply {
            findViewById<TextView>(R.id.icon_description).apply {
                HtmlUtils.setTextWithNiceLinks(this, uiModel.iconAboutText)
            }
            aboutIcon = this
        }
    }

    private fun getAboutAppPage(parent: ViewGroup): View {
        return aboutPlaid ?: parent.inflateView(R.layout.about_plaid)
            .apply {
                findViewById<TextView>(R.id.about_description).apply {
                    HtmlUtils.setTextWithNiceLinks(this, uiModel.appAboutText)
                }
                aboutPlaid = this
            }
    }

    private fun getAboutLibsPage(parent: ViewGroup): View {
        return aboutLibs ?: parent.inflateView(R.layout.about_libs).apply {
            findViewById<RecyclerView>(R.id.libs_list).apply {
                adapter = LibraryAdapter(uiModel.librariesUiModel)
            }
            aboutLibs = this
        }
    }
}
