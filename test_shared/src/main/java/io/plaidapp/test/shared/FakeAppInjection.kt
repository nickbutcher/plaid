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

import io.plaidapp.core.data.CoroutinesDispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Unconfined

// Using Unconfined here as a way to execute coroutines in tests in the test thread.
// That makes the test to wait for the coroutine to finish before carry on.
// This needs to be improved in the future, either using a CoroutinesMainDispatcherRule
// or the recently added TestCoroutineDispatcher when it hits stable.
fun provideFakeCoroutinesDispatcherProvider(
    main: CoroutineDispatcher = Unconfined,
    computation: CoroutineDispatcher = Unconfined,
    io: CoroutineDispatcher = Unconfined
): CoroutinesDispatcherProvider = CoroutinesDispatcherProvider(main, computation, io)
