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

package io.plaidapp.about.ui.model

import `in`.uncod.android.bypass.Markdown
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.plaidapp.about.ui.AboutStyler
import javax.inject.Inject

/**
 * Factory to create [AboutViewModel]
 */
class AboutViewModelFactory @Inject constructor(
    private val aboutStyler: AboutStyler,
    private val resources: Resources,
    private val markdown: Markdown
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass != AboutViewModel::class.java) {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
        return AboutViewModel(
            aboutStyler,
            resources,
            markdown
        ) as T
    }
}
