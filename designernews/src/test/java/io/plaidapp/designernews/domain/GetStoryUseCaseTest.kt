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

package io.plaidapp.designernews.domain

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.designernews.storyLinks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.Exception
import java.util.Date
import java.util.GregorianCalendar

/**
 * Tests for [GetStoryUseCase] mocking all the dependencies.
 */
class GetStoryUseCaseTest {
    private val createdDate: Date = GregorianCalendar(2018, 1, 13).time
    private val storyId = 45L
    private val storyResponse =
        StoryResponse(
            id = storyId,
            title = "Plaid 2.0 was released",
            created_at = createdDate,
            links = storyLinks
        )
    private val story =
        Story(
            id = storyId,
            title = "Plaid 2.0 was released",
            createdAt = createdDate,
            userId = storyLinks.user,
            links = storyLinks
        )

    private val storiesRepository: StoriesRepository = mock()
    private val getStoryUseCase = GetStoryUseCase(storiesRepository)

    @Test
    fun getStory_whenStoryInRepository() {
        // Given that the repository returns a story request for the id
        whenever(storiesRepository.getStory(storyId)).thenReturn(Result.Success(storyResponse))

        // When getting the story
        val result = getStoryUseCase(storyId)

        // The story is returned
        assertEquals(Result.Success(story), result)
    }

    @Test
    fun getStory_whenStoryNotInRepository() {
        // Given that the repository returns with error
        whenever(storiesRepository.getStory(storyId)).thenReturn(Result.Error(Exception("exception")))

        // When getting the story
        val result = getStoryUseCase(storyId)

        // Error is return
        assertTrue(result is Result.Error)
    }
}
