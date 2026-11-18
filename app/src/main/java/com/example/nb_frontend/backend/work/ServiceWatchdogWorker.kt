package com.example.nb_frontend.backend.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nb_frontend.backend.service.MonitorController

class ServiceWatchdogWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        MonitorController.ensureRunning(applicationContext)
        return Result.success()
    }
}
