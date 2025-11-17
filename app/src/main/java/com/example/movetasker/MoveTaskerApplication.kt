package com.example.movetasker

import android.app.Application
import com.example.movetasker.data.local.AppDatabase
import com.example.movetasker.data.repository.MoveTaskerRepository
import com.example.movetasker.util.MoveTaskerPreferenceManager

class MoveTaskerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: MoveTaskerRepository by lazy {
        MoveTaskerRepository(database.ruleDao(), database.moveLogDao())
    }
    val preferenceManager: MoveTaskerPreferenceManager by lazy {
        MoveTaskerPreferenceManager(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
