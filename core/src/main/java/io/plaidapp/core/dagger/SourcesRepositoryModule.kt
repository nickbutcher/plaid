/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import io.plaidapp.core.R
import io.plaidapp.core.dagger.scope.FeatureScope
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import io.plaidapp.core.data.SourceItem
import io.plaidapp.core.data.prefs.SourcesLocalDataSource
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem.Companion.SOURCE_DESIGNER_NEWS_POPULAR
import io.plaidapp.core.designernews.data.DesignerNewsSourceItem
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem

/**
 * Module to provide [SourcesRepository].
 */
@Module
class SourcesRepositoryModule {

    @Provides
    @FeatureScope
    fun provideSourceRepository(
        context: Context,
        dispatcherProvider: CoroutinesDispatcherProvider
    ): SourcesRepository {
        val defaultSources = provideDefaultSources(context)
        val sharedPrefs = context.getSharedPreferences(SOURCES_PREF, Context.MODE_PRIVATE)
        val localDataSource = SourcesLocalDataSource(sharedPrefs)
        return SourcesRepository.getInstance(defaultSources, localDataSource, dispatcherProvider)
    }

    private fun provideDefaultSources(context: Context): List<SourceItem> {
        val defaultDesignerNewsSourceName = context.getString(R.string.source_designer_news_popular)
        val defaultDribbbleSourceName = context.getString(R.string
                .source_dribbble_search_material_design)
        val defaultProductHuntSourceName = context.getString(R.string.source_product_hunt)

        val defaultSources = mutableListOf<SourceItem>()
        defaultSources.add(
            DesignerNewsSourceItem(
                SOURCE_DESIGNER_NEWS_POPULAR,
                SOURCE_DESIGNER_NEWS_POPULAR,
                100,
                defaultDesignerNewsSourceName,
                true)
        )
        // 200 sort order range left for DN searches
        defaultSources.add(DribbbleSourceItem(defaultDribbbleSourceName, true))
        // 400 sort order range left for dribbble searches
        defaultSources.add(ProductHuntSourceItem(defaultProductHuntSourceName))
        return defaultSources
    }

    companion object {
        private const val SOURCES_PREF = "SOURCES_PREF"
    }
}
