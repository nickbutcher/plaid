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

package io.plaidapp.designernews

import android.content.Context
import io.plaidapp.core.designernews.provideLoginRepository
import io.plaidapp.core.designernews.provideVotesRepository
import io.plaidapp.core.provideCoroutinesContextProvider
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import io.plaidapp.designernews.ui.DesignerNewsViewModelFactory

/**
 * File providing different dependencies.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideViewModelFactory(context: Context): DesignerNewsViewModelFactory {
    return DesignerNewsViewModelFactory(
        provideLoginRepository(context),
        provideUpvoteStoryUseCase(context),
        provideUpvoteCommentUseCase(context),
        provideCoroutinesContextProvider()
    )
}

fun provideUpvoteStoryUseCase(context: Context): UpvoteStoryUseCase {
    val loginRepository = provideLoginRepository(context)
    val votesRepository = provideVotesRepository(context)
    return UpvoteStoryUseCase(loginRepository, votesRepository)
}

fun provideUpvoteCommentUseCase(context: Context): UpvoteCommentUseCase {
    val loginRepository = provideLoginRepository(context)
    val votesRepository = provideVotesRepository(context)
    return UpvoteCommentUseCase(loginRepository, votesRepository)
}
