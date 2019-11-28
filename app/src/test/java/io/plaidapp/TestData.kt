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

package io.plaidapp

import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.data.stories.model.StoryLinks
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.ui.filter.SourceUiModel
import java.util.Date
import java.util.GregorianCalendar

val designerNewsSource = DesignerNewsSearchSourceItem(
    "query",
    true
)
val designerNewsSourceUiModel = SourceUiModel(
    id = "id",
    key = designerNewsSource.key,
    name = designerNewsSource.name,
    active = designerNewsSource.active,
    iconRes = designerNewsSource.iconRes,
    isSwipeDismissable = designerNewsSource.isSwipeDismissable,
    onSourceClicked = {},
    onSourceDismissed = {}
)
val dribbbleSource = DribbbleSourceItem("dribbble", true)

val post = Post(
    id = 345L,
    title = "Plaid",
    url = "www.plaid.amazing",
    tagline = "amazing",
    discussionUrl = "www.disc.plaid",
    redirectUrl = "www.d.plaid",
    commentsCount = 5,
    votesCount = 100
)

val player = User(
    id = 1L,
    name = "Nick Butcher",
    username = "nickbutcher",
    avatarUrl = "www.prettyplaid.nb"
)

val shot = Shot(
    id = 1L,
    title = "Foo Nick",
    page = 0,
    description = "",
    images = Images(),
    user = player
).apply {
    dataSource = dribbbleSource.key
}

const val userId = 5L
const val storyId = 1345L
val createdDate: Date = GregorianCalendar(2018, 1, 13).time
val commentIds = listOf(11L, 12L)
val storyLinks = StoryLinks(
    user = userId,
    comments = commentIds,
    upvotes = emptyList(),
    downvotes = emptyList()
)

val story = Story(
    id = storyId,
    title = "Plaid 2.0 was released",
    page = 0,
    createdAt = createdDate,
    userId = userId,
    links = storyLinks
).apply {
    dataSource = designerNewsSource.key
}
