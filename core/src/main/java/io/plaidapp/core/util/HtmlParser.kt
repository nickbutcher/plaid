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

package io.plaidapp.core.util

import android.content.res.ColorStateList
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import androidx.annotation.ColorInt

class HtmlParser {

    /**
     * Parse the given input using [TouchableUrlSpan]s rather than vanilla [URLSpan]s
     * so that they respond to touch.
     */
    fun parse(
        input: String,
        linkTextColor: ColorStateList,
        @ColorInt linkHighlightColor: Int
    ): SpannableStringBuilder {
        var spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY) as SpannableStringBuilder
        } else {
            Html.fromHtml(input) as SpannableStringBuilder
        }

        // strip any trailing newlines
        while (spanned.isNotEmpty() && spanned[spanned.length - 1] == '\n') {
            spanned = spanned.delete(spanned.length - 1, spanned.length)
        }

        return HtmlUtils.linkifyPlainLinks(spanned, linkTextColor, linkHighlightColor)
    }
}
