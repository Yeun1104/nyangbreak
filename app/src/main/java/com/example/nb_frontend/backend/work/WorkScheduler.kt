package com.example.nb_frontend.backend.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nb_frontend.backend.util.DateUtil
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val DAILY_CLEANUP = "daily_cleanup"
    private const val WATCHDOG = "service_watchdog"

    fun scheduleAll(context: Context) {
        val wm = WorkManager.getInstance(context)

        val cleanup = PeriodicWorkRequestBuilder<DailyCleanupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(DateUtil.millisUntilNextMidnight() + TimeUnit.MINUTES.toMillis(1), TimeUnit.MILLISECONDS)
            .build()
        wm.enqueueUniquePeriodicWork(DAILY_CLEANUP, ExistingPeriodicWorkPolicy.KEEP, cleanup)

        val watchdog = PeriodicWorkRequestBuilder<ServiceWatchdogWorker>(15, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork(WATCHDOG, ExistingPeriodicWorkPolicy.KEEP, watchdog)
    }
}
