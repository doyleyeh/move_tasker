package com.example.autosorter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.FileObserver
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.autosorter.MoveTaskerApplication
import com.example.autosorter.R
import com.example.autosorter.data.entity.MoveStatus
import com.example.autosorter.data.entity.RuleEntity
import com.example.autosorter.data.repository.AutosortRepository
import com.example.autosorter.util.FileMover
import com.example.autosorter.util.FileRuleEvaluator
import com.example.autosorter.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import java.io.File

class FileWatchService : Service() {
    private val channelId = "move_tasker_monitor_channel"
    private val notificationId = 101
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: AutosortRepository

    private val observers = mutableMapOf<Long, DirectoryObserver>()

    override fun onCreate() {
        super.onCreate()
        val app = application as MoveTaskerApplication
        repository = app.repository
        startForeground(notificationId, createNotification())
        serviceScope.launch {
            repository.getEnabledRules().collectLatest { rules ->
                rebuildObservers(rules)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!PermissionHelper.hasAllFilesAccess(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        observers.values.forEach { it.stopWatching() }
        observers.clear()
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Move Tasker Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring folders for new files"
            }
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Move Tasker")
            .setContentText("Monitoring folders for new files...")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
    }

    private fun rebuildObservers(rules: List<RuleEntity>) {
        observers.values.forEach { it.stopWatching() }
        observers.clear()
        rules.filter { it.enabled && it.sourcePath.isNotBlank() && it.destinationPath.isNotBlank() }
            .forEach { rule ->
            val observer = DirectoryObserver(rule) { entity, file ->
                handleFile(entity, file)
            }
            observer.startWatching()
            observers[rule.id] = observer
        }
    }

    private fun handleFile(rule: RuleEntity, file: File) {
        serviceScope.launch {
            if (!FileRuleEvaluator.matches(rule, file)) return@launch
            val destinationDir = File(rule.destinationPath)
            try {
                val movedFile = FileMover.moveFile(file, destinationDir)
                repository.saveMoveResult(
                    rule = rule,
                    sourceFilePath = file.absolutePath,
                    destinationFilePath = movedFile.absolutePath,
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

    private inner class DirectoryObserver(
        private val rule: RuleEntity,
        private val onMatchedFile: (RuleEntity, File) -> Unit
    ) : FileObserver(rule.sourcePath, CREATE or MOVED_TO or CLOSE_WRITE) {
        override fun onEvent(event: Int, path: String?) {
            if (path.isNullOrBlank()) return
            val file = File(rule.sourcePath, path)
            if (!file.exists()) return
            onMatchedFile(rule, file)
        }
    }
}
