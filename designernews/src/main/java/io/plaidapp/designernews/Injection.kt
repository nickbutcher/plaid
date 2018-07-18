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

package io.plaidapp.designernews

import android.content.Context
import io.plaidapp.core.designernews.provideDesignerNewsLoginRepository
import io.plaidapp.core.provideCoroutinesContextProvider
import io.plaidapp.designernews.ui.ViewModelFactory

/**
 * File providing different dependencies.
 *
 * Once we have a dependency injection framework or a service locator, this should be removed.
 */

fun provideViewModelFactory(context: Context): ViewModelFactory {
    return ViewModelFactory(
        provideDesignerNewsLoginRepository(context),
        provideCoroutinesContextProvider()
    )
}
