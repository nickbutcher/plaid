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

package io.plaidapp.core.data

import kotlinx.coroutines.ObsoleteCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Provide coroutines context.
 *
 * Prefer usage of [CoroutinesDispatcherProvider]
 */
@Deprecated(
    message = "Use CoroutinesDispatcherProvider instead",
    replaceWith = ReplaceWith(
        expression = "CoroutinesDispatcherProvider",
        imports = ["io.plaidapp.core.data.CoroutinesDispatcherProvider"]
    )
)
@ObsoleteCoroutinesApi
data class CoroutinesContextProvider(
    val main: CoroutineContext,
    val computation: CoroutineContext,
    val io: CoroutineContext
) {

    @Inject constructor(dispatchers: CoroutinesDispatcherProvider) : this(dispatchers.main, dispatchers.computation, dispatchers.io)
}
