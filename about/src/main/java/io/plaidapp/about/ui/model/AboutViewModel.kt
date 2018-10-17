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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.res.ColorStateList
import android.content.res.Resources
import android.text.Layout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import androidx.core.text.toSpannable
import io.plaidapp.about.R
import io.plaidapp.about.domain.model.Library
import io.plaidapp.about.ui.AboutStyler
import io.plaidapp.core.util.event.Event
import io.plaidapp.core.util.plusAssign
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

/**
 * [ViewModel] for the [io.plaidapp.about.ui.AboutActivity].
 */
internal class AboutViewModel @Inject constructor(
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
            "https://avatars.githubusercontent.com/u/32689599",
            false
        ),
        Library(
            "android-ktx",
            "A set of Kotlin extensions for Android app development.",
            "https://android.googlesource.com/platform/frameworks/support/",
            "https://avatars.githubusercontent.com/u/32689599",
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
            "Crashlytics",
            "The most powerful, yet lightest weight crash reporting solution.",
            "https://firebase.google.com/products/crashlytics/",
            "https://get.fabric.io/assets/logo_crashlytics-9b74c30b45657fa925e1659ff5e550a8.png",
            false
        ),
        Library(
            "Dagger2",
            "Dagger is a fully static, compile-time dependency injection framework" +
                    "for both Java and Android.",
            "https://google.github.io/dagger/",
            "https://avatars.githubusercontent.com/u/1342004",
            true
        ),
        Library(
            "Firebase",
            "A comprehensive mobile development platform",
            "https://firebase.google.com/",
            "https://www.gstatic.com/mobilesdk/160503_mobilesdk/logo/2x/firebase_96dp.png",
            false
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
            "ktlint",
            "An anti-bikeshedding Kotlin linter with built-in formatter",
            "https://github.com/shyiko/ktlint",
            "https://avatars.githubusercontent.com/u/370176",
            true
        ),
        Library(
            "Mockito",
            "Tasty mocking framework for unit tests in Java",
            "http://site.mockito.org/",
            "https://raw.githubusercontent.com/mockito/mockito/master/src/javadoc/org/mockito/logo.png",
            false

        ),
        Library(
            "Mockito-Kotlin",
            "A small library that provides helper functions to work with Mockito in Kotlin.",
            "https://github.com/nhaarman/mockito-kotlin",
            "https://avatars.githubusercontent.com/u/3015152",
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
        ),
        Library(
            "Kotlin Coroutine Adapter",
            "A Retrofit 2 CallAdapter.Factory for Kotlin coroutine's Deferred.",
            "https://github.com/JakeWharton/retrofit2-kotlin-coroutines-adapter",
            "https://avatars.githubusercontent.com/u/66577",
            true
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
