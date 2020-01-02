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

package io.plaidapp.core.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Class that knows how to store locally sources keys, together with their active state
 */
class SourcesLocalDataSource(private val prefs: SharedPreferences) {

    /**
     * Get all sources
     */
    fun getKeys(): Set<String>? = getMutableKeys()

    /**
     * Add a source and set the active state for the source
     */
    fun addSource(source: String, isActive: Boolean) {
        val sources = getMutableKeys(mutableSetOf())
        sources?.add(source)
        prefs.edit {
            putStringSet(KEY_SOURCES, sources)
            putBoolean(source, isActive)
        }
    }

    /**
     * Update the active state of a source
     */
    fun updateSource(source: String, isActive: Boolean) {
        prefs.edit { putBoolean(source, isActive) }
    }

    /**
     * Remove source and the corresponding active state.
     * @return true if the source was removed, false otherwise
     */
    fun removeSource(source: String): Boolean {
        var removed = false
        val sources = getMutableKeys(mutableSetOf())
        sources?.remove(source)
        prefs.edit {
            putStringSet(KEY_SOURCES, sources)
            remove(source)
            removed = true
        }
        return removed
    }

    fun getSourceActiveState(source: String): Boolean {
        return prefs.getBoolean(source, false)
    }

    private fun getMutableKeys(default: Set<String>? = null): MutableSet<String>? {
        return prefs.getStringSet(KEY_SOURCES, default)
    }

    companion object {
        private const val KEY_SOURCES = "KEY_SOURCES"
    }
}
