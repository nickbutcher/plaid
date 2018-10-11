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

package io.plaidapp.core.dagger

import dagger.Module
import dagger.Provides
import io.plaidapp.core.data.CoroutinesContextProvider
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.android.UI

/**
 * Provide [CoroutinesContextProvider] to this app's components.
 */
@Module class CoroutinesContextProviderModule {

    @Provides fun provideCoroutinesContextProvider() = CoroutinesContextProvider(UI, CommonPool)
}
