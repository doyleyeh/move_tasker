package com.example.movetasker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.movetasker.service.FileWatchService
import com.example.movetasker.worker.RuleScanWorker

object MoveTaskerController {
    fun startMonitoring(context: Context) {
        if (!PermissionHelper.hasAllFilesAccess(context)) return
        val intent = Intent(context, FileWatchService::class.java)
        ContextCompat.startForegroundService(context, intent)
        RuleScanWorker.schedule(context)
    }

    fun stopMonitoring(context: Context) {
        context.stopService(Intent(context, FileWatchService::class.java))
        RuleScanWorker.cancel(context)
    }

    fun rescheduleWorker(context: Context) {
        RuleScanWorker.schedule(context)
    }

    fun cancelWorker(context: Context) {
        RuleScanWorker.cancel(context)
    }
}
