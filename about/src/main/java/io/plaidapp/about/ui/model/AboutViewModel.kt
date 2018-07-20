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

package io.plaidapp.about.ui.model

import `in`.uncod.android.bypass.Markdown
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.res.ColorStateList
import android.content.res.Resources
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import androidx.core.text.plusAssign
import androidx.core.text.toSpannable
import io.plaidapp.about.R
import io.plaidapp.about.domain.model.Library
import io.plaidapp.about.ui.AboutStyler
import io.plaidapp.core.util.event.Event
import kotlin.LazyThreadSafetyMode.NONE

/**
 * [ViewModel] for the [io.plaidapp.about.ui.AboutActivity].
 */
internal class AboutViewModel(
    private val aboutStyler: AboutStyler,
    private val resources: Resources,
    private val markdown: Markdown
) : ViewModel() {

    private val _navigationTarget = MutableLiveData<Event<String>>()
    val navigationTarget: LiveData<Event<String>>
        get() = _navigationTarget

    private val appAboutText: CharSequence by lazy(NONE) {
        with(aboutStyler) {
            // fun with spans & markdown
            val about0 = getSpannableFromMarkdown(
                R.string.about_plaid_0,
                linksColor,
                highlightColor
            )

            val about1 = SpannableString(resources.getString(R.string.about_plaid_1))
            about1 += AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)

            val about2 = getSpannableFromMarkdown(
                R.string.about_plaid_2,
                linksColor,
                highlightColor
            )
            about2 += AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)

            val about3 = getSpannableFromMarkdown(
                R.string.about_plaid_3,
                linksColor, highlightColor
            )
            about3 += AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)
            spannableFrom(about0, "\n\n", about1, "\n", about2, "\n\n", about3)
        }
    }

    private val iconAboutText: CharSequence by lazy(NONE) {

        val icon0 = resources.getString(R.string.about_icon_0)
        with(aboutStyler) {
            val icon1 = getSpannableFromMarkdown(R.string.about_icon_1, linksColor, highlightColor)
            spannableFrom(icon0, "\n", icon1)
        }
    }

    @VisibleForTesting
    internal val libraries = listOf(
        Library(
            "Android support libraries",
            "The Android support libraries offer a number of features that are " +
                    "not built into the framework.",
            "https://developer.android.com/topic/libraries/support-library",
            "https://developer.android.com/images/android_icon_125.png",
            false
        ),
        Library(
            "Bypass",
            "Skip the HTML, Bypass takes markdown and renders it directly.",
            "https://github.com/Uncodin/bypass",
            "https://avatars.githubusercontent.com/u/1072254",
            true
        ),
        Library(
            "Glide",
            "An image loading and caching library for Android focused onsmooth " +
                    "scrolling.",
            "https://github.com/bumptech/glide",
            "https://avatars.githubusercontent.com/u/423539",
            false
        ),
        Library(
            "JSoup",
            "Java HTML Parser, with best of DOM, CSS, and jquery.",
            "https://github.com/jhy/jsoup/",
            "https://avatars.githubusercontent.com/u/76934",
            true
        ),
        Library(
            "OkHttp",
            "An HTTP & HTTP/2 client for Android and Java applications.",
            "http://square.github.io/okhttp/",
            "https://avatars.githubusercontent.com/u/82592",
            false
        ),
        Library(
            "Retrofit",
            "A type-safe HTTP client for Android and Java.",
            "http://square.github.io/retrofit/",
            "https://avatars.githubusercontent.com/u/82592",
            false
        )
    )

    val uiModel by lazy(NONE) {
        AboutUiModel(
            appAboutText,
            iconAboutText,
            LibrariesUiModel(libraries) {
                onLibraryClick(it)
            })
    }

    @VisibleForTesting
    internal fun onLibraryClick(library: Library) {
        _navigationTarget.value = Event(library.link)
    }

    private fun getSpannableFromMarkdown(
        @StringRes stringId: Int,
        linksColor: ColorStateList,
        @ColorInt highlightColor: Int
    ): Spannable {
        return markdown.markdownToSpannable(
            resources.getString(stringId), linksColor,
            highlightColor, null
        ).toSpannable()
    }

    private fun spannableFrom(vararg text: CharSequence): CharSequence {
        return SpannableStringBuilder().apply {
            text.forEach { append(it) }
        }
    }
}
