package com.example.autosorter.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.autosorter.util.AutosortController
import com.example.autosorter.util.AutosortPreferenceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = AutosortPreferenceManager(context)
            if (prefs.isAutosortEnabled()) {
                AutosortController.startMonitoring(context)
            }
        }
    }
}
