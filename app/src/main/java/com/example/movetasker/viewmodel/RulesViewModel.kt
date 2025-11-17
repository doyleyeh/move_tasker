package com.example.movetasker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.movetasker.MoveTaskerApplication
import com.example.movetasker.data.entity.FileTypeFilter
import com.example.movetasker.data.entity.MoveStatus
import com.example.movetasker.data.entity.RuleEntity
import com.example.movetasker.data.repository.MoveTaskerRepository
import com.example.movetasker.util.MoveTaskerPreferenceManager
import com.example.movetasker.util.FileMover
import com.example.movetasker.util.FileRuleEvaluator
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RulesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MoveTaskerRepository
    private val preferenceManager: MoveTaskerPreferenceManager

    val rules: StateFlow<List<RuleEntity>>
    val moveTaskerEnabled: StateFlow<Boolean>

    init {
        val app = getApplication<MoveTaskerApplication>()
        repository = app.repository
        preferenceManager = app.preferenceManager
        rules = repository.getRules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        moveTaskerEnabled = preferenceManager.moveTaskerEnabledFlow()
            .stateIn(viewModelScope, SharingStarted.Eagerly, preferenceManager.isMoveTaskerEnabled())
    }

    fun toggleRule(rule: RuleEntity, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateRule(rule.copy(enabled = enabled))
        }
    }

    fun deleteRule(rule: RuleEntity) {
        viewModelScope.launch { repository.deleteRule(rule) }
    }

    fun saveRule(
        id: Long,
        name: String,
        source: String,
        destination: String,
        typeFilter: FileTypeFilter,
        extensions: String?,
        enabled: Boolean
    ) {
        if (name.isBlank() || source.isBlank() || destination.isBlank()) return
        val rule = RuleEntity(
            id = id,
            name = name,
            sourcePath = source,
            destinationPath = destination,
            fileTypeFilter = typeFilter,
            extensionsFilter = extensions?.trim()?.ifEmpty { null },
            enabled = enabled
        )
        viewModelScope.launch {
            if (id == 0L) repository.insertRule(rule) else repository.updateRule(rule)
        }
    }

    fun observeRule(id: Long) = repository.observeRule(id)

    fun setMoveTaskerEnabled(enabled: Boolean) {
        preferenceManager.setMoveTaskerEnabled(enabled)
    }

    fun runRuleOnce(rule: RuleEntity, onComplete: (Int, Int) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { executeRuleOnce(rule) }
            onComplete(result.first, result.second)
        }
    }

    private suspend fun executeRuleOnce(rule: RuleEntity): Pair<Int, Int> {
        val sourceDir = File(rule.sourcePath)
        if (!sourceDir.exists() || !sourceDir.isDirectory) return 0 to 0
        val files = sourceDir.listFiles()?.filter { FileRuleEvaluator.matches(rule, it) } ?: emptyList()
        var moved = 0
        var failed = 0
        files.forEach { file ->
            try {
                val movedFile = FileMover.moveFile(file, File(rule.destinationPath))
                repository.saveMoveResult(
                    rule = rule,
                    sourceFilePath = file.absolutePath,
                    destinationFilePath = movedFile.absolutePath,
                    status = MoveStatus.SUCCESS
                )
                moved++
            } catch (t: Throwable) {
                failed++
                repository.saveMoveResult(
                    rule = rule,
                    sourceFilePath = file.absolutePath,
                    destinationFilePath = File(rule.destinationPath, file.name).absolutePath,
                    status = MoveStatus.FAILED,
                    error = t.message
                )
            }
        }
        return moved to failed
    }
}
