/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.util;

import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import in.uncod.android.bypass.LoadImageCallback;
import in.uncod.android.bypass.Markdown;
import in.uncod.android.bypass.style.TouchableUrlSpan;

/**
 * Utility methods for working with HTML.
 */
public class HtmlUtils {

    private HtmlUtils() {
    }

    /**
     * Work around some 'features' of TextView and URLSpans. i.e. vanilla URLSpans do not react to
     * touch so we replace them with our own {@link TouchableUrlSpan}
     * & {@link LinkTouchMovementMethod} to fix this.
     * <p/>
     * Setting a custom MovementMethod on a TextView also alters touch handling (see
     * TextView#fixFocusableAndClickableSettings) so we need to correct this.
     */
    public static void setTextWithNiceLinks(TextView textView, CharSequence input) {
        textView.setText(input);
        textView.setMovementMethod(LinkTouchMovementMethod.getInstance());
        textView.setFocusable(false);
        textView.setClickable(false);
        textView.setLongClickable(false);
    }

    static SpannableStringBuilder linkifyPlainLinks(
            CharSequence input,
            ColorStateList linkTextColor,
            @ColorInt int linkHighlightColor) {
        final SpannableString plainLinks = new SpannableString(input); // copy of input

        // Linkify doesn't seem to work as expected on M+
        // TODO: figure out why
        //Linkify.addLinks(plainLinks, Linkify.WEB_URLS);

        final URLSpan[] urlSpans = plainLinks.getSpans(0, plainLinks.length(), URLSpan.class);

        // add any plain links to the output
        final SpannableStringBuilder ssb = new SpannableStringBuilder(input);
        for (URLSpan urlSpan : urlSpans) {
            ssb.removeSpan(urlSpan);
            ssb.setSpan(new TouchableUrlSpan(urlSpan.getURL(), linkTextColor, linkHighlightColor),
                    plainLinks.getSpanStart(urlSpan),
                    plainLinks.getSpanEnd(urlSpan),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    /**
     * Parse Markdown and plain-text links.
     * <p/>
     * {@link Markdown} does not handle plain text links (i.e. not md syntax) and requires a
     * {@code String} input (i.e. squashes any spans). {@link Linkify} handles plain links but also
     * removes any existing spans. So we can't just run our input through both.
     * <p/>
     * Instead we use the markdown lib, then take a copy of the output and Linkify
     * <strong>that</strong>. We then find any {@link URLSpan}s and add them to the markdown output.
     * Best of both worlds.
     */
    public static CharSequence parseMarkdownAndPlainLinks(
            String input,
            Markdown markdown,
            ColorStateList linkTextColors,
            @ColorInt int highlightColor,
            LoadImageCallback loadImageCallback) {
        CharSequence markedUp = markdown.markdownToSpannable(input, linkTextColors, highlightColor,
                loadImageCallback);
        return linkifyPlainLinks(markedUp, linkTextColors, highlightColor);
    }

}
