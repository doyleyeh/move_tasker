package com.example.autosorter.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AutosortPreferenceManager(context: Context) {
    companion object {
        private const val PREF_NAME = "autosort_prefs"
        private const val KEY_AUTOSORT_ENABLED = "autosort_enabled"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val autosortFlow = MutableStateFlow(isAutosortEnabled())

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_AUTOSORT_ENABLED) {
            autosortFlow.value = isAutosortEnabled()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setAutosortEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTOSORT_ENABLED, enabled).apply()
    }

    fun isAutosortEnabled(): Boolean = prefs.getBoolean(KEY_AUTOSORT_ENABLED, true)

    fun autosortEnabledFlow(): StateFlow<Boolean> = autosortFlow
}
