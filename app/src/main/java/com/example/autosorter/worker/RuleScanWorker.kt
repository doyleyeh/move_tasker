package com.example.autosorter.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.autosorter.data.local.AppDatabase
import com.example.autosorter.data.repository.AutosortRepository
import com.example.autosorter.data.entity.MoveStatus
import com.example.autosorter.data.entity.RuleEntity
import com.example.autosorter.util.FileMover
import com.example.autosorter.util.FileRuleEvaluator
import java.io.File
import java.util.concurrent.TimeUnit

class RuleScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private val repository: AutosortRepository by lazy {
        val db = AppDatabase.getInstance(applicationContext)
        AutosortRepository(db.ruleDao(), db.moveLogDao())
    }

    override suspend fun doWork(): Result {
        val rules = repository.getEnabledRulesOnce()
        rules.forEach { rule ->
            processRule(rule)
        }
        return Result.success()
    }

    private suspend fun processRule(rule: RuleEntity) {
        val sourceDir = File(rule.sourcePath)
        if (!sourceDir.exists() || !sourceDir.isDirectory) return
        if (rule.destinationPath.isBlank()) return
        sourceDir.listFiles()?.forEach { file ->
            if (!FileRuleEvaluator.matches(rule, file)) return@forEach
            if (repository.hasLogForSource(file.absolutePath)) return@forEach
            val destinationDir = File(rule.destinationPath)
            try {
                val moved = FileMover.moveFile(file, destinationDir)
                repository.saveMoveResult(
                    rule = rule,
                    sourceFilePath = file.absolutePath,
                    destinationFilePath = moved.absolutePath,
                    status = MoveStatus.SUCCESS
                )
            } catch (t: Throwable) {
                repository.saveMoveResult(
                    rule = rule,
                    sourceFilePath = file.absolutePath,
                    destinationFilePath = File(destinationDir, file.name).absolutePath,
                    status = MoveStatus.FAILED,
                    error = t.message
                )
            }
        }
    }

    companion object {
        private const val WORK_NAME = "rule-scan-worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RuleScanWorker>(30, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
