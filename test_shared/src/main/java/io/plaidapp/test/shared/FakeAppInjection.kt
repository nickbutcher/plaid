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

package io.plaidapp.test.shared

import io.plaidapp.core.data.CoroutinesContextProvider
import io.plaidapp.core.data.CoroutinesDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlin.coroutines.CoroutineContext

fun provideFakeCoroutinesDispatcherProvider(dispatcher: CoroutineDispatcher = Unconfined): CoroutinesDispatcherProvider =
    CoroutinesDispatcherProvider(dispatcher, dispatcher, dispatcher)

/**
 * Use CoroutineContext when the use of TestCoroutineContext is necessary for testing,
 * replace with TestCoroutineDispatcher when available.
 *
 * @see https://github.com/Kotlin/kotlinx.coroutines/pull/890
 */
fun provideFakeCoroutinesContextProvider(context: CoroutineContext = Unconfined): CoroutinesContextProvider =
    CoroutinesContextProvider(context, context, context)
