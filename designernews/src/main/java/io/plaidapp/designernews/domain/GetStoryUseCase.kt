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

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryResponse
import io.plaidapp.core.designernews.data.stories.model.toStory

/**
 * Use case that gets a [StoryResponse]s from [StoriesRepository] and transforms it to [Story]
 */
class GetStoryUseCase(private val storiesRepository: StoriesRepository) {

    operator fun invoke(id: Long): Result<Story> {
        val result = storiesRepository.getStory(id)
        return if (result is Result.Success) {
            Result.Success(result.data.toStory())
        } else {
            Result.Error(IllegalStateException("Story $id not cached"))
        }
    }
}
