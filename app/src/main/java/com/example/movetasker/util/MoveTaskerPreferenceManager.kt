package com.example.movetasker.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MoveTaskerPreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "movetasker_prefs"
        private const val LEGACY_PREF_NAME = "autosort_prefs"
        private const val KEY_MOVETASKER_ENABLED = "movetasker_enabled"
        private const val LEGACY_KEY_AUTOSORT_ENABLED = "autosort_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val legacyPrefs: SharedPreferences =
        context.getSharedPreferences(LEGACY_PREF_NAME, Context.MODE_PRIVATE)

    private val moveTaskerFlow = MutableStateFlow(isMoveTaskerEnabled())

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_MOVETASKER_ENABLED || key == LEGACY_KEY_AUTOSORT_ENABLED) {
            moveTaskerFlow.value = isMoveTaskerEnabled()
        }
    }

    init {
        migrateLegacyPreferenceIfNeeded()
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setMoveTaskerEnabled(enabled: Boolean) {
        prefs.edit().apply {
            putBoolean(KEY_MOVETASKER_ENABLED, enabled)
            apply()
        }
        legacyPrefs.edit().apply {
            remove(LEGACY_KEY_AUTOSORT_ENABLED)
            apply()
        }
    }

    fun isMoveTaskerEnabled(): Boolean {
        if (prefs.contains(KEY_MOVETASKER_ENABLED)) {
            return prefs.getBoolean(KEY_MOVETASKER_ENABLED, true)
        }
        val legacyValue = legacyPrefs.getBoolean(LEGACY_KEY_AUTOSORT_ENABLED, true)
        if (legacyPrefs.contains(LEGACY_KEY_AUTOSORT_ENABLED)) {
            prefs.edit().apply {
                putBoolean(KEY_MOVETASKER_ENABLED, legacyValue)
                apply()
            }
            legacyPrefs.edit().apply {
                remove(LEGACY_KEY_AUTOSORT_ENABLED)
                apply()
            }
        }
        return legacyValue
    }

    fun moveTaskerEnabledFlow(): StateFlow<Boolean> = moveTaskerFlow

    private fun migrateLegacyPreferenceIfNeeded() {
        if (!prefs.contains(KEY_MOVETASKER_ENABLED) && legacyPrefs.contains(LEGACY_KEY_AUTOSORT_ENABLED)) {
            val legacyValue = legacyPrefs.getBoolean(LEGACY_KEY_AUTOSORT_ENABLED, true)
            prefs.edit().apply {
                putBoolean(KEY_MOVETASKER_ENABLED, legacyValue)
                apply()
            }
            legacyPrefs.edit().apply {
                remove(LEGACY_KEY_AUTOSORT_ENABLED)
                apply()
            }
        }
    }
}
