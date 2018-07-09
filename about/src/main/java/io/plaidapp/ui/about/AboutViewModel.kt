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

import `in`.uncod.android.bypass.Bypass
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.res.ColorStateList
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import io.plaidapp.about.R
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.event.Event
import io.plaidapp.R as appR

/**
 * [AndroidViewModel] for the about module.
 */
class AboutViewModel(application: Application) : AndroidViewModel(application) {

    private val markdown = Bypass(application, Bypass.Options())
    private val resources = application.resources

    private val linksColor = ContextCompat.getColorStateList(application,
            appR.color.plaid_links)!!
    private val highlightColor = ColorUtils.getThemeColor(application,
            appR.attr.colorPrimary, appR.color.primary)

    private val _navigationTarget = MutableLiveData<Event<String>>()
    val navigationTarget: LiveData<Event<String>>
        get() = _navigationTarget

    val appAboutText: CharSequence
        get() {
            // fun with spans & markdown
            val about0 = getSpannableFromMarkdown(R.string.about_plaid_0,
                    linksColor,
                    highlightColor)

            val about1 = SpannableString(resources.getString(R.string.about_plaid_1)).apply {
                setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            val about2 = SpannableString(
                    getSpannableFromMarkdown(R.string.about_plaid_2,
                            linksColor, highlightColor)).apply {
                setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            val about3 = SpannableString(
                    getSpannableFromMarkdown(R.string.about_plaid_3,
                            linksColor, highlightColor)).apply {
                setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                        0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return "$about0\n\n$about1\n$about2\n\n$about3"
        }

    val iconAboutText: CharSequence
        get() {
            val icon0 = resources.getString(R.string.about_icon_0)
            val icon1 = getSpannableFromMarkdown(R.string.about_icon_1, linksColor, highlightColor)
            return "$icon0\n$icon1"
        }

    internal fun onLibraryClick(library: Library) {
        _navigationTarget.value = Event(library.link)
    }

    internal val libraries = listOf(
            Library("Android support libraries",
                    "The Android support libraries offer a number of features that are " +
                            "not built into the framework.",
                    "https://developer.android.com/topic/libraries/support-library",
                    "https://developer.android.com/images/android_icon_125.png",
                    false),
            Library("Bypass",
                    "Skip the HTML, Bypass takes markdown and renders it directly.",
                    "https://github.com/Uncodin/bypass",
                    "https://avatars.githubusercontent.com/u/1072254",
                    true),
            Library("Glide",
                    "An image loading and caching library for Android focused onsmooth " +
                            "scrolling.",
                    "https://github.com/bumptech/glide",
                    "https://avatars.githubusercontent.com/u/423539",
                    false),
            Library("JSoup",
                    "Java HTML Parser, with best of DOM, CSS, and jquery.",
                    "https://github.com/jhy/jsoup/",
                    "https://avatars.githubusercontent.com/u/76934",
                    true),
            Library("OkHttp",
                    "An HTTP & HTTP/2 client for Android and Java applications.",
                    "http://square.github.io/okhttp/",
                    "https://avatars.githubusercontent.com/u/82592",
                    false),
            Library("Retrofit",
                    "A type-safe HTTP client for Android and Java.",
                    "http://square.github.io/retrofit/",
                    "https://avatars.githubusercontent.com/u/82592",
                    false))

    private fun getSpannableFromMarkdown(
        @StringRes stringId: Int,
        linksColor: ColorStateList,
        @ColorInt highlightColor: Int
    ): CharSequence {
        return markdown.markdownToSpannable(resources.getString(stringId), linksColor,
                highlightColor, null)
    }
}
