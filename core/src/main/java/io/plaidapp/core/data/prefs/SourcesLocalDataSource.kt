package io.plaidapp.core.data.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

/**
 * Class that knows how to store locally sources, together with their active state
 */
class SourcesLocalDataSource @Inject constructor(private val prefs: SharedPreferences) {

    /**
     * Get all sources
     */
    fun getSources(): Set<String>? = getMutableSources()

    /**
     * Add a source and set the active state for the source
     */
    fun addSource(source: String, isActive: Boolean) {
        val sources = getMutableSources(mutableSetOf())
        sources?.add(source)
        prefs.edit {
            putStringSet(KEY_SOURCES, sources)
            putBoolean(source, isActive)
        }
    }

    /**
     * Add a set of sources and a common active state
     */
    fun addSources(sources: Set<String>, isActive: Boolean) {
        prefs.edit {
            putStringSet(KEY_SOURCES, sources)
            sources.forEach { putBoolean(it, isActive) }
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
        var removed = false;
        val sources = getMutableSources(mutableSetOf())
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

    private fun getMutableSources(default: Set<String>? = null): MutableSet<String>? {
        return prefs.getStringSet(KEY_SOURCES, default)
    }

    companion object {
        val SOURCES_PREF = "SOURCES_PREF"
        private val KEY_SOURCES = "KEY_SOURCES"
    }
}
