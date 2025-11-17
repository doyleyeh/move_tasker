package com.example.movetasker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movetasker.MoveTaskerApplication
import com.example.movetasker.data.entity.MoveLogWithRule
import com.example.movetasker.data.repository.MoveTaskerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LogsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MoveTaskerRepository = getApplication<MoveTaskerApplication>().repository

    val logs: StateFlow<List<MoveLogWithRule>> = repository.getRecentLogsWithRule(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
