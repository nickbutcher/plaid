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

package io.plaidapp.search

import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User

/**
 * Search test data
 */

val player1 = User(
    id = 1L,
    name = "Nick Butcher",
    username = "nickbutcher",
    avatarUrl = "www.prettyplaid.nb"
)

val player2 = User(
    id = 142L,
    name = "Florina",
    username = "flo",
    avatarUrl = "www.prettyplaid.fm"
)

val testShot1 = Shot(
    id = 1L,
    title = "Foo Nick",
    page = 0,
    description = "",
    images = Images(),
    user = player1
)

val testShot2 = Shot(
    id = 199L,
    title = "Foo Flo",
    page = 0,
    description = "",
    images = Images(),
    user = player2
)

val shots = listOf(testShot1, testShot2)
