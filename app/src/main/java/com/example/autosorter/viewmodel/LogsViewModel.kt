package com.example.autosorter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.autosorter.MoveTaskerApplication
import com.example.autosorter.data.entity.MoveLogWithRule
import com.example.autosorter.data.repository.AutosortRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LogsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AutosortRepository = getApplication<MoveTaskerApplication>().repository

    val logs: StateFlow<List<MoveLogWithRule>> = repository.getRecentLogsWithRule(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
