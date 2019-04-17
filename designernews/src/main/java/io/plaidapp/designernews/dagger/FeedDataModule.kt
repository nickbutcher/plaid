/*
 * Copyright 2019 Google, Inc.
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

import android.content.Context
import dagger.Module
import dagger.Provides
import io.plaidapp.core.R
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.DesignerNewsSourceItem
import io.plaidapp.core.designernews.data.stories.StoriesRepository
import io.plaidapp.core.interfaces.PlaidDataSource
import io.plaidapp.designernews.domain.search.DesignerNewsDataSource

@Module
class FeedDataModule {

    @Provides
    @FeatureScope
    fun defaultFeedDataSources(
        context: Context,
        sourcesRepository: StoriesRepository
    ): Set<PlaidDataSource> {
        val defaultDesignerNewsSourceName = context.getString(R.string.source_designer_news_popular)
        val sourceItem = DesignerNewsSourceItem(
            DesignerNewsSearchSourceItem.SOURCE_DESIGNER_NEWS_POPULAR,
            DesignerNewsSearchSourceItem.SOURCE_DESIGNER_NEWS_POPULAR,
            100,
            defaultDesignerNewsSourceName,
            true
        )
        val dnDataSource = DesignerNewsDataSource(sourceItem, sourcesRepository)
        return setOf(dnDataSource)
    }
}
