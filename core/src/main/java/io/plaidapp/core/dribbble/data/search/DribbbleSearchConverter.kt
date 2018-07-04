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

package io.plaidapp.core.dribbble.data.search

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern

private const val HOST = "https://dribbble.com"
private val PATTERN_PLAYER_ID = Pattern.compile("users/(\\d+?)/", Pattern.DOTALL)
private val DATE_FORMAT = SimpleDateFormat("MMMM d, yyyy")

/**
 * Dribbble API does not have a search endpoint so we have to do gross things :(
 */
object DribbbleSearchConverter : Converter<ResponseBody, List<Shot>> {

    /** Factory for creating converter. We only care about decoding responses.  */
    class Factory : Converter.Factory() {

        override fun responseBodyConverter(
            type: Type?,
            annotations: Array<Annotation>?,
            retrofit: Retrofit?
        ): Converter<ResponseBody, *>? {
            return DribbbleSearchConverter
        }
    }

    override fun convert(value: ResponseBody): List<Shot> {
        val shotElements = Jsoup.parse(value.string(), HOST).select("li[id^=screenshot]")
        return shotElements.map { parseShot(it) }
    }

    private fun parseShot(element: Element): Shot {
        val id = element.id().replace("screenshot-", "").toLong()
        val htmlUrl = HOST + element.select("a.dribbble-link").first().attr("href")
        val descriptionBlock = element.select("a.dribbble-over").first()
        val title = descriptionBlock.select("strong").first().text()
        // API responses wrap description in a <p> tag. Do the same for consistent display.
        var description = descriptionBlock.select("span.comment").text().trim { it <= ' ' }
        if (!description.isNullOrEmpty()) {
            description = "<p>$description</p>"
        }
        var imgUrl = element.select("img").first().attr("src")
        if (imgUrl.contains("_teaser.")) {
            imgUrl = imgUrl.replace("_teaser.", ".")
        }
        val animated = element.select("div.gif-indicator").first() != null
        val createdAt: Date? = try {
            DATE_FORMAT.parse(descriptionBlock.select("em.timestamp").first().text())
        } catch (e: ParseException) {
            null
        }
        val likesCount = element.select("li.fav").first().child(0).text().replace(",", "").toLong()
        val viewsCount = element.select("li.views").first().child(0).text().replace(",", "").toLong()
        val player = parsePlayer(element.select("h2").first())

        return Shot(
            id = id,
            htmlUrl = htmlUrl,
            title = title,
            description = description,
            images = Images(normal = imgUrl),
            animated = animated,
            createdAt = createdAt,
            likesCount = likesCount,
            viewsCount = viewsCount,
            user = player
        )
    }

    private fun parsePlayer(element: Element): User {
        val userBlock = element.select("a.url").first()
        var avatarUrl = userBlock.select("img.photo").first().attr("src")
        if (avatarUrl.contains("/mini/")) {
            avatarUrl = avatarUrl.replace("/mini/", "/normal/")
        }
        val matchId = PATTERN_PLAYER_ID.matcher(avatarUrl)
        var id: Long = -1L
        if (matchId.find() && matchId.groupCount() == 1) {
            id = java.lang.Long.parseLong(matchId.group(1))
        }
        val slashUsername = userBlock.attr("href")
        val username = slashUsername.substring(1)
        val name = userBlock.text()

        return User(
            id = id,
            name = name,
            username = username,
            avatarUrl = avatarUrl
        )
    }
}
