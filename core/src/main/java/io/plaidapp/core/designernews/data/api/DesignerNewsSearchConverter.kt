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

package io.plaidapp.core.designernews.data.api

import java.lang.reflect.Type
import java.util.regex.Pattern
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter
import retrofit2.Retrofit

private val PATTERN_STORY_ID = Pattern.compile("https:\\/\\/www\\.designernews\\.co\\/stories\\/([0-9]*)")

/**
 * Designer News API doesn't have a search endpoint, so we have to scrape the HTML ourselves.
 *
 * This extracts the IDs from the HTML and returns them as a list. We can use these IDs with the API to get the rest of
 * the information that we need.
 */
object DesignerNewsSearchConverter : Converter<ResponseBody, List<String>> {

    /** Factory for creating converter. We only care about decoding responses.  */
    class Factory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>?,
            retrofit: Retrofit?
        ): Converter<ResponseBody, *>? {
            return if (annotations != null && annotations.any { it is DesignerNewsSearch }) {
                DesignerNewsSearchConverter
            } else {
                null
            }
        }
    }

    override fun convert(body: ResponseBody): List<String> {
        val searchResults = Jsoup.parse(body.string(), DesignerNewsService.ENDPOINT)
            .select("li.search-page-result > a")
        return searchResults.mapNotNull { parseSearchResult(it) }
    }

    private fun parseSearchResult(searchResult: Element): String? {
        if (searchResult.select(".search-result-content-type").text() != "story") {
            return null
        }

        val idMatcher = PATTERN_STORY_ID.matcher(searchResult.attr("href"))
        return if (idMatcher.find() && idMatcher.groupCount() == 1) {
            idMatcher.group(1)
        } else {
            null
        }
    }
}
