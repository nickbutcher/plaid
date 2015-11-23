/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.util;

import android.content.res.ColorStateList;
import android.support.annotation.ColorInt;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.widget.TextView;

import in.uncod.android.bypass.style.TouchableUrlSpan;

/**
 * Utility methods for working with HTML.
 */
public class HtmlUtils {

    private HtmlUtils() { }

    /**
     * Work around some 'features' of TextView and URLSpans. i.e. vanilla URLSpans do not react to
     * touch so we replace them with our own {@link io.plaidapp.ui.span
     * .TouchableUrlSpan}
     * & {@link io.plaidapp.util.LinkTouchMovementMethod} to fix this.
     * <p/>
     * Setting a custom MovementMethod on a TextView also alters touch handling (see
     * TextView#fixFocusableAndClickableSettings) so we need to correct this.
     *
     * @param textView
     * @param input
     */
    public static void setTextWithNiceLinks(TextView textView, CharSequence input) {
        textView.setText(input);
        textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
        textView.setFocusable(false);
        textView.setClickable(false);
        textView.setLongClickable(false);
    }

    /**
     * Parse the given input using {@link TouchableUrlSpan}s
     * rather than vanilla {@link android.text.style.URLSpan}s so that they respond to touch.
     *
     * @param input
     * @param linkTextColor
     * @param linkHighlightColor
     * @return
     */
    public static Spanned parseHtml(String input,
                                    ColorStateList linkTextColor,
                                    @ColorInt int linkHighlightColor) {
        SpannableStringBuilder spanned = (SpannableStringBuilder) Html.fromHtml(input);

        // strip any trailing newlines
        while (spanned.charAt(spanned.length() - 1) == '\n') {
            spanned = spanned.delete(spanned.length() - 1, spanned.length());
        }

        URLSpan[] urlSpans = spanned.getSpans(0, spanned.length(), URLSpan.class);
        for (URLSpan urlSpan : urlSpans) {
            int start = spanned.getSpanStart(urlSpan);
            int end = spanned.getSpanEnd(urlSpan);
            spanned.removeSpan(urlSpan);
            // spanned.subSequence(start, start + 1) == "@" TODO send to our own user activity...
            // when i've written it
            spanned.setSpan(new TouchableUrlSpan(urlSpan.getURL(), linkTextColor,
                    linkHighlightColor), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spanned;
    }

}
