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

package io.plaidapp.core.designernews.data.stories.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.GregorianCalendar

class StoryResponseTest {

    val createdDate: Date = GregorianCalendar(1997, 12, 28).time

    @Test
    fun story_converted() {
        // Given a story response
        val storyLinks = StoryLinks(
            user = 123L,
            comments = listOf(1, 2, 3),
            upvotes = listOf(11, 22, 33),
            downvotes = listOf(111, 222, 333)
        )
        val storyResponse = StoryResponse(
            id = 987,
            title = "My Plaid story",
            comment = "This is amazing",
            comment_html = "www.plaid.com",
            comment_count = 0,
            vote_count = 100,
            created_at = createdDate,
            links = storyLinks
        )

        // When converting it to a story
        val story = storyResponse.toStory()

        // The story has the expected data
        val expectedStory = Story(
            id = 987,
            title = "My Plaid story",
            comment = "This is amazing",
            commentHtml = "www.plaid.com",
            commentCount = 0,
            voteCount = 100,
            userId = 123L,
            createdAt = createdDate,
            links = storyLinks,
            userDisplayName = null,
            userPortraitUrl = null,
            userJob = null
        )
        assertEquals(expectedStory, story)
    }
}
