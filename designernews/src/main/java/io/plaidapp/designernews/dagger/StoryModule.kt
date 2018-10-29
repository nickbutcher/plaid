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

package io.plaidapp.designernews.dagger

import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.MarkdownModule
import io.plaidapp.core.dagger.SharedPreferencesModule
import io.plaidapp.core.dagger.designernews.DesignerNewsDataModule
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.comments.CommentsRemoteDataSource
import io.plaidapp.core.designernews.data.comments.CommentsRepository
import io.plaidapp.core.designernews.data.login.LoginRepository
import io.plaidapp.core.designernews.data.stories.StoriesRepository
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
import io.plaidapp.designernews.ui.login.LoginViewModel
import io.plaidapp.designernews.ui.story.StoryActivity
import io.plaidapp.designernews.ui.story.StoryViewModel
import io.plaidapp.designernews.ui.story.StoryViewModelFactory

/**
 * Dagger module for [StoryActivity].
 */
@Module(
    includes = [CoreDataModule::class,
        DesignerNewsDataModule::class,
        MarkdownModule::class,
        SharedPreferencesModule::class]
)
class StoryModule(private val storyId: Long, private val activity: StoryActivity) {

    @Provides
    fun provideLoginViewModel(
        factory: DesignerNewsViewModelFactory
    ): LoginViewModel =
        ViewModelProviders.of(activity, factory).get(LoginViewModel::class.java)

    @Provides
    fun provideStoryViewModel(
        factory: StoryViewModelFactory
    ): StoryViewModel =
        ViewModelProviders.of(activity, factory).get(StoryViewModel::class.java)

    @Provides
    fun provideViewModelFactory(
        loginRepository: LoginRepository,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): DesignerNewsViewModelFactory =
        DesignerNewsViewModelFactory(loginRepository, coroutinesDispatcherProvider)

    @Provides
    fun provideStoryViewModelFactory(
        getStoryUseCase: GetStoryUseCase,
        postStoryCommentUseCase: PostStoryCommentUseCase,
        postReplyUseCase: PostReplyUseCase,
        commentsWithRepliesAndUsersUseCase: GetCommentsWithRepliesAndUsersUseCase,
        upvoteStoryUseCase: UpvoteStoryUseCase,
        upvoteCommentUseCase: UpvoteCommentUseCase,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): StoryViewModelFactory =
        StoryViewModelFactory(
                storyId,
                getStoryUseCase,
                postStoryCommentUseCase,
                postReplyUseCase,
                commentsWithRepliesAndUsersUseCase,
                upvoteStoryUseCase,
                upvoteCommentUseCase,
                coroutinesDispatcherProvider
        )

    @Provides
    fun provideGetStoryUseCase(repository: StoriesRepository): GetStoryUseCase =
        GetStoryUseCase(repository)

    @Provides
    fun provideUpvoteStoryUseCase(
        loginRepository: LoginRepository,
        votesRepository: VotesRepository
    ): UpvoteStoryUseCase =
        UpvoteStoryUseCase(loginRepository, votesRepository)

    @Provides
    fun provideUpvoteCommentUseCase(
        loginRepository: LoginRepository,
        votesRepository: VotesRepository
    ): UpvoteCommentUseCase =
        UpvoteCommentUseCase(loginRepository, votesRepository)

    @Provides
    fun provideCommentsWithRepliesUseCase(
        commentsRepository: CommentsRepository
    ): GetCommentsWithRepliesUseCase =
        GetCommentsWithRepliesUseCase(commentsRepository)

    @Provides
    fun provideCommentsWithRepliesAndUsersUseCase(
        useCase: GetCommentsWithRepliesUseCase,
        userRepository: UserRepository
    ): GetCommentsWithRepliesAndUsersUseCase =
        GetCommentsWithRepliesAndUsersUseCase(useCase, userRepository)

    @Provides
    fun provideUserRemoteDataSource(service: DesignerNewsService): UserRemoteDataSource =
        UserRemoteDataSource(service)

    @Provides
    fun provideUserRepository(dataSource: UserRemoteDataSource): UserRepository =
        UserRepository.getInstance(dataSource)

    @Provides
    fun provideCommentsRemoteDataSource(service: DesignerNewsService): CommentsRemoteDataSource =
        CommentsRemoteDataSource.getInstance(service)

    @Provides
    fun providePostReplyUseCase(
        commentsRepository: CommentsRepository,
        loginRepository: LoginRepository
    ): PostReplyUseCase =
        PostReplyUseCase(commentsRepository, loginRepository)

    @Provides
    fun providePostStoryCommentUseCase(
        commentsRepository: CommentsRepository,
        loginRepository: LoginRepository
    ): PostStoryCommentUseCase =
        PostStoryCommentUseCase(commentsRepository, loginRepository)

    @Provides
    fun provideVotesRemoteDataSource(service: DesignerNewsService): VotesRemoteDataSource =
        VotesRemoteDataSource(service)

    @Provides
    fun provideVotesRepository(remoteDataSource: VotesRemoteDataSource): VotesRepository =
        VotesRepository.getInstance(remoteDataSource)
}
