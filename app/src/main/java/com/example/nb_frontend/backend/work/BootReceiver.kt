package com.example.nb_frontend.backend.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nb_frontend.backend.service.MonitorController

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        MonitorController.ensureRunning(context)
        WorkScheduler.scheduleAll(context)
    }
}
