package com.example.nb_frontend.backend.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.PowerManager

/** UsageEvents로 지금 화면에 떠 있는 앱을 판별한다 */
class ForegroundAppDetector(context: Context) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var currentPackage: String? = null
    private var lastQueryEnd = 0L

    fun detect(now: Long = System.currentTimeMillis()): String? {
        if (!power.isInteractive) return null

        val begin = if (lastQueryEnd == 0L) now - INITIAL_LOOKBACK_MS else lastQueryEnd - OVERLAP_MS
        val events = try {
            usm.queryEvents(begin, now)
        } catch (e: SecurityException) {
            return null
        } ?: return currentPackage

        val event = UsageEvents.Event()
        var latestTs = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.timeStamp < latestTs) continue
            when (event.eventType) {
                RESUMED -> {
                    currentPackage = event.packageName
                    latestTs = event.timeStamp
                }
                PAUSED -> if (event.packageName == currentPackage) {
                    currentPackage = null
                    latestTs = event.timeStamp
                }
            }
        }
        lastQueryEnd = now
        return currentPackage
    }

    companion object {
        private const val INITIAL_LOOKBACK_MS = 60 * 60 * 1000L
        private const val OVERLAP_MS = 2_000L

        @Suppress("DEPRECATION")
        private val RESUMED =
            if (Build.VERSION.SDK_INT >= 29) UsageEvents.Event.ACTIVITY_RESUMED else UsageEvents.Event.MOVE_TO_FOREGROUND
        @Suppress("DEPRECATION")
        private val PAUSED =
            if (Build.VERSION.SDK_INT >= 29) UsageEvents.Event.ACTIVITY_PAUSED else UsageEvents.Event.MOVE_TO_BACKGROUND
    }
}
