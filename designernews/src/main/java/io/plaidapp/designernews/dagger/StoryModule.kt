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

package io.plaidapp.designernews.dagger

import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.data.comments.CommentsRemoteDataSource
import io.plaidapp.designernews.data.comments.CommentsRepository
import io.plaidapp.designernews.data.users.UserRemoteDataSource
import io.plaidapp.designernews.data.users.UserRepository
import io.plaidapp.designernews.domain.GetCommentsWithRepliesAndUsersUseCase
import io.plaidapp.designernews.domain.GetStoryUseCase
import io.plaidapp.designernews.domain.PostReplyUseCase
import io.plaidapp.designernews.domain.PostStoryCommentUseCase
import io.plaidapp.designernews.ui.DesignerNewsViewModelFactory
import io.plaidapp.designernews.ui.login.LoginViewModel
import io.plaidapp.designernews.ui.story.StoryActivity
import io.plaidapp.designernews.ui.story.StoryViewModel
import io.plaidapp.designernews.ui.story.StoryViewModelFactory

/**
 * Dagger module for [StoryActivity].
 */
@Module
class StoryModule(private val storyId: Long, private val activity: StoryActivity) {

    @Provides
    fun provideLoginViewModel(
        factory: DesignerNewsViewModelFactory
    ): LoginViewModel =
        ViewModelProvider(activity, factory).get(LoginViewModel::class.java)

    @Provides
    fun provideStoryViewModel(
        factory: StoryViewModelFactory
    ): StoryViewModel =
        ViewModelProvider(activity, factory).get(StoryViewModel::class.java)

    @Provides
    fun provideStoryViewModelFactory(
        getStoryUseCase: GetStoryUseCase,
        postStoryCommentUseCase: PostStoryCommentUseCase,
        postReplyUseCase: PostReplyUseCase,
        commentsWithRepliesAndUsersUseCase: GetCommentsWithRepliesAndUsersUseCase,
        coroutinesDispatcherProvider: CoroutinesDispatcherProvider
    ): StoryViewModelFactory =
        StoryViewModelFactory(
            storyId,
            getStoryUseCase,
            postStoryCommentUseCase,
            postReplyUseCase,
            commentsWithRepliesAndUsersUseCase,
            coroutinesDispatcherProvider
        )

    @Provides
    @FeatureScope
    fun provideUserRepository(dataSource: UserRemoteDataSource): UserRepository =
        UserRepository(dataSource)

    @Provides
    @FeatureScope
    fun provideCommentsRemoteDataSource(service: DesignerNewsService): CommentsRemoteDataSource =
        CommentsRemoteDataSource(service)

    @Provides
    @FeatureScope
    fun provideCommentsRepository(dataSource: CommentsRemoteDataSource): CommentsRepository =
        CommentsRepository(dataSource)
}
