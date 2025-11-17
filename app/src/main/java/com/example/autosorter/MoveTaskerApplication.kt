package com.example.autosorter

import android.app.Application
import com.example.autosorter.data.local.AppDatabase
import com.example.autosorter.data.repository.AutosortRepository
import com.example.autosorter.util.AutosortPreferenceManager

class MoveTaskerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AutosortRepository by lazy {
        AutosortRepository(database.ruleDao(), database.moveLogDao())
    }
    val preferenceManager: AutosortPreferenceManager by lazy {
        AutosortPreferenceManager(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
