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
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.comments.CommentsRemoteDataSource
import io.plaidapp.core.designernews.data.comments.CommentsRepository
import io.plaidapp.core.designernews.provideCommentsRepository
import io.plaidapp.core.designernews.provideDesignerNewsService
import io.plaidapp.core.designernews.provideLoginRepository
import io.plaidapp.core.designernews.provideStoriesRepository
import io.plaidapp.core.provideCoroutinesDispatcherProvider
import io.plaidapp.designernews.data.users.UserRemoteDataSource
import io.plaidapp.designernews.data.users.UserRepository
import io.plaidapp.designernews.data.votes.VotesRemoteDataSource
import io.plaidapp.designernews.data.votes.VotesRepository
import io.plaidapp.designernews.domain.GetCommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetCommentsWithRepliesUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.PostStoryCommentUseCase
import io.plaidapp.designernews.domain.UpvoteCommentUseCase
import io.plaidapp.designernews.domain.UpvoteStoryUseCase
import io.plaidapp.designernews.ui.DesignerNewsViewModelFactory
import io.plaidapp.designernews.ui.story.StoryViewModelFactory

/**
 * File providing different dependencies.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideViewModelFactory(context: Context): DesignerNewsViewModelFactory {
    return DesignerNewsViewModelFactory(
        provideLoginRepository(context),
        provideCoroutinesDispatcherProvider()
    )
}

fun provideStoryViewModelFactory(storyId: Long, context: Context): StoryViewModelFactory {
    return StoryViewModelFactory(
        storyId,
        provideGetStoryUseCase(context),
        providePostStoryCommentUseCase(context),
        providePostReplyUseCase(context),
        provideCommentsWithRepliesAndUsersUseCase(context),
        provideUpvoteStoryUseCase(context),
        provideUpvoteCommentUseCase(context),
        provideCoroutinesDispatcherProvider()
    )
}

fun provideGetStoryUseCase(context: Context) = GetStoryUseCase(provideStoriesRepository(context))

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

fun provideCommentsWithRepliesAndUsersUseCase(context: Context): GetCommentsWithRepliesAndUsersUseCase {
    val service = provideDesignerNewsService(context)
    val commentsRepository = provideCommentsRepository(
        provideCommentsRemoteDataSource(service)
    )
    val userRepository = provideUserRepository(provideUserRemoteDataSource(service))
    return provideCommentsWithRepliesAndUsersUseCase(
        provideCommentsWithRepliesUseCase(commentsRepository),
        userRepository
    )
}

fun provideCommentsWithRepliesUseCase(commentsRepository: CommentsRepository) =
    GetCommentsWithRepliesUseCase(commentsRepository)

fun provideCommentsWithRepliesAndUsersUseCase(
    commentsWithGetCommentsWithReplies: GetCommentsWithRepliesUseCase,
    userRepository: UserRepository
) = GetCommentsWithRepliesAndUsersUseCase(commentsWithGetCommentsWithReplies, userRepository)

private fun provideUserRemoteDataSource(service: DesignerNewsService) =
    UserRemoteDataSource(service)

private fun provideUserRepository(dataSource: UserRemoteDataSource) =
    UserRepository.getInstance(dataSource)

private fun provideCommentsRemoteDataSource(service: DesignerNewsService) =
    CommentsRemoteDataSource.getInstance(service)

fun providePostReplyUseCase(context: Context): PostReplyUseCase {
    val service = provideDesignerNewsService(context)
    val commentsRepository = provideCommentsRepository(
        provideCommentsRemoteDataSource(service)
    )
    val loginRepository = provideLoginRepository(context)
    return PostReplyUseCase(commentsRepository, loginRepository)
}

fun providePostStoryCommentUseCase(context: Context): PostStoryCommentUseCase {
    val service = provideDesignerNewsService(context)
    val commentsRepository = provideCommentsRepository(
        provideCommentsRemoteDataSource(service)
    )
    val loginRepository = provideLoginRepository(context)
    return PostStoryCommentUseCase(commentsRepository, loginRepository)
}

fun provideVotesRepository(context: Context): VotesRepository {
    return provideVotesRepository(
        provideVotesRemoteDataSource(provideDesignerNewsService(context))
    )
}

private fun provideVotesRemoteDataSource(service: DesignerNewsService) =
    VotesRemoteDataSource(service)

private fun provideVotesRepository(remoteDataSource: VotesRemoteDataSource) =
    VotesRepository.getInstance(remoteDataSource)
