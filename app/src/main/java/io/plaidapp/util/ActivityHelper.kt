/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

@file:JvmName("ActivityHelper")

package io.plaidapp.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.customtabs.CustomTabsIntent
import android.support.customtabs.CustomTabsSession
import android.support.v4.content.ContextCompat
import io.plaidapp.R
import io.plaidapp.data.api.designernews.UpvoteStoryService

private const val PACKAGE_NAME = "io.plaidapp.ui"

fun intentTo(addressableActivity: AddressableActivity): Intent {
    return Intent(Intent.ACTION_VIEW).setClassName(
            "io.plaidapp",
            addressableActivity.className)
}

interface AddressableActivity {
    val className: String
}

object Activities {

    object About : AddressableActivity {
        override val className = "$PACKAGE_NAME.AboutActivity"
    }

    object DesignerNews {
        object Login : AddressableActivity {
            override val className = "$PACKAGE_NAME.DesignerNewsLogin"
        }

        object Story : AddressableActivity {
            override val className = "$PACKAGE_NAME.DesignerNewsStory"
            const val EXTRA_STORY = "story"

            fun customTabIntent(
                    context: Context,
                    story: io.plaidapp.data.api.designernews.model.Story,
                    session: CustomTabsSession
            ): CustomTabsIntent.Builder {
                val upvoteStory = Intent(context, UpvoteStoryService::class.java)
                upvoteStory.action = UpvoteStoryService.ACTION_UPVOTE
                upvoteStory.putExtra(UpvoteStoryService.EXTRA_STORY_ID, story.id)
                val pendingIntent = PendingIntent.getService(context, 0, upvoteStory, 0)

                return CustomTabsIntent.Builder(session)
                        .setToolbarColor(ContextCompat.getColor(context, R.color.designer_news))
                        .setActionButton(drawableToBitmap(context,
                                R.drawable.ic_upvote_filled_24dp_white)!!,
                                context.getString(R.string.upvote_story),
                                pendingIntent,
                                false)
                        .setShowTitle(true)
                        .enableUrlBarHiding()
                        .addDefaultShareMenuItem()
            }
        }

        object PostStory : AddressableActivity {
            override val className = "$PACKAGE_NAME.PostNewDesignerNewsStory"
            const val RESULT_DRAG_DISMISSED = 3
            const val RESULT_POSTING = 4
        }
    }

    object Dribbble {
        object Login : AddressableActivity {
            override val className = "$PACKAGE_NAME.DribbbleLogin"
        }

        object Shot : AddressableActivity {
            override val className = "$PACKAGE_NAME.DribbbleShot"
            const val EXTRA_SHOT = "EXTRA_SHOT"
            const val RESULT_EXTRA_SHOT_ID = "RESULT_EXTRA_SHOT_ID"
        }
    }

    object Player : AddressableActivity {
        override val className = "$PACKAGE_NAME.PlayerActivity"
        const val EXTRA_PLAYER = "EXTRA_PLAYER"
        const val EXTRA_PLAYER_NAME = "EXTRA_PLAYER_NAME"
        const val EXTRA_PLAYER_ID = "EXTRA_PLAYER_ID"
        const val EXTRA_PLAYER_USERNAME = "EXTRA_PLAYER_USERNAME"
    }

    object Search : AddressableActivity {
        override val className = "$PACKAGE_NAME.SearchActivity"

        const val EXTRA_QUERY = "EXTRA_QUERY"
        const val EXTRA_SAVE_DRIBBBLE = "EXTRA_SAVE_DRIBBBLE"
        const val EXTRA_SAVE_DESIGNER_NEWS = "EXTRA_SAVE_DESIGNER_NEWS"
        const val RESULT_CODE_SAVE = 7
    }

}