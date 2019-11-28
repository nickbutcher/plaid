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

@file:JvmName("Injector")

package io.plaidapp.designernews.dagger

import `in`.uncod.android.bypass.Bypass
import android.util.TypedValue
import io.plaidapp.core.dagger.MarkdownModule
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.designernews.R
import io.plaidapp.designernews.ui.login.LoginActivity
import io.plaidapp.designernews.ui.story.StoryActivity
import io.plaidapp.ui.coreComponent

/**
 * Inject [StoryActivity].
 */
fun inject(storyId: Long, activity: StoryActivity) {

    val bypassOptions = Bypass.Options()
        .setBlockQuoteLineColor(activity.getColor(R.color.thread_depth))
        .setBlockQuoteLineWidth(2) // dps
        .setBlockQuoteLineIndent(8) // dps
        .setPreImageLinebreakHeight(4) // dps
        .setBlockQuoteIndentSize(TypedValue.COMPLEX_UNIT_DIP, 2f)
        .setBlockQuoteTextColor(
            ColorUtils.getThemeColor(
                activity,
                android.R.attr.textColorSecondary
            )
        )

    DaggerStoryComponent.builder()
        .coreComponent(activity.coreComponent())
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
        .coreComponent(activity.coreComponent())
        .sharedPreferencesModule(DesignerNewsPreferencesModule(activity))
        .build()
        .inject(activity)
}
