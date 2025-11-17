package com.example.movetasker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.movetasker.util.MoveTaskerController
import com.example.movetasker.util.MoveTaskerPreferenceManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = MoveTaskerPreferenceManager(context)
            if (prefs.isMoveTaskerEnabled()) {
                MoveTaskerController.startMonitoring(context)
            }
        }
    }
}
