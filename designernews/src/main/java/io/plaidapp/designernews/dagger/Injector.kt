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

@file:JvmName("Injector")

package io.plaidapp.designernews.dagger

import `in`.uncod.android.bypass.Bypass
import android.util.TypedValue
import androidx.core.content.ContextCompat
import io.plaidapp.core.dagger.CoreDataModule
import io.plaidapp.core.dagger.CoroutinesDispatcherProviderModule
import io.plaidapp.core.dagger.MarkdownModule
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.designernews.ui.login.LoginActivity
import io.plaidapp.designernews.ui.story.StoryActivity
import io.plaidapp.ui.PlaidApplication
import retrofit2.converter.gson.GsonConverterFactory

private val coreDataModule =
    CoreDataModule(DesignerNewsService.ENDPOINT, GsonConverterFactory.create())

/**
 * Inject [StoryActivity].
 */
fun inject(storyId: Long, activity: StoryActivity) {

    val coreComponent = PlaidApplication.coreComponent(activity)

    val bypassOptions = Bypass.Options()
        .setBlockQuoteLineColor(
            ContextCompat.getColor(activity, io.plaidapp.R.color.designer_news_quote_line)
        )
        .setBlockQuoteLineWidth(2) // dps
        .setBlockQuoteLineIndent(8) // dps
        .setPreImageLinebreakHeight(4) // dps
        .setBlockQuoteIndentSize(TypedValue.COMPLEX_UNIT_DIP, 2f)
        .setBlockQuoteTextColor(
            ContextCompat.getColor(activity, io.plaidapp.R.color.designer_news_quote)
        )

    DaggerStoryComponent.builder()
        .coreComponent(coreComponent)
        .coroutinesDispatcherProviderModule(CoroutinesDispatcherProviderModule())
        .coreDataModule(coreDataModule)
        .designerNewsModule(StoryModule(storyId, activity))
        .markdownModule(MarkdownModule(activity.resources.displayMetrics, bypassOptions))
        .sharedPreferencesModule(
            DesignerNewsPreferencesModule(activity)
        )
        .build()
        .inject(activity)
}

fun inject(activity: LoginActivity) {

    DaggerLoginComponent.builder()
        .coreDataModule(coreDataModule)
        .sharedPreferencesModule(
            DesignerNewsPreferencesModule(activity)
        )
        .build()
        .inject(activity)
}
