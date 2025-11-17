package com.example.autosorter.util

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.autosorter.service.FileWatchService
import com.example.autosorter.worker.RuleScanWorker

object AutosortController {
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
