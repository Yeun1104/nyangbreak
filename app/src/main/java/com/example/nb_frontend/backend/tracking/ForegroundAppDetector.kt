package com.example.nb_frontend.backend.tracking

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.util.Log

/** UsageEvents로 지금 화면에 떠 있는 앱을 판별한다 */
class ForegroundAppDetector(context: Context) {
    private val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val power = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private var currentPackage: String? = null
    private var lastQueryEnd = 0L
    private var calls = 0

    fun detect(now: Long = System.currentTimeMillis()): String? {
        if (!power.isInteractive) return null

        // 현재 앱을 모르면 이벤트를 놓쳤을 수 있으니 넓게 다시 조회한다
        val begin = if (currentPackage == null || lastQueryEnd == 0L) now - LOOKBACK_MS else lastQueryEnd - OVERLAP_MS
        val events = try {
            usm.queryEvents(begin, now)
        } catch (e: SecurityException) {
            Log.w(TAG, "사용 기록 조회 권한 없음", e)
            return null
        } ?: return currentPackage

        val event = UsageEvents.Event()
        var latestTs = 0L
        var count = 0
        var lastResumed: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            count++
            if (event.timeStamp < latestTs) continue
            when (event.eventType) {
                RESUMED -> {
                    currentPackage = event.packageName
                    lastResumed = event.packageName
                    latestTs = event.timeStamp
                }
                PAUSED -> if (event.packageName == currentPackage) {
                    currentPackage = null
                    latestTs = event.timeStamp
                }
            }
        }
        lastQueryEnd = now
        if (calls++ % LOG_EVERY == 0) {
            Log.d(TAG, "조회 ${(now - begin) / 1000}초, 이벤트 ${count}개, 마지막 RESUMED=$lastResumed, 현재=$currentPackage")
        }
        return currentPackage
    }

    companion object {
        private const val TAG = "ForegroundAppDetector"
        private const val LOOKBACK_MS = 60 * 60 * 1000L
        private const val OVERLAP_MS = 2_000L
        private const val LOG_EVERY = 10

        @Suppress("DEPRECATION")
        private val RESUMED =
            if (Build.VERSION.SDK_INT >= 29) UsageEvents.Event.ACTIVITY_RESUMED else UsageEvents.Event.MOVE_TO_FOREGROUND
        @Suppress("DEPRECATION")
        private val PAUSED =
            if (Build.VERSION.SDK_INT >= 29) UsageEvents.Event.ACTIVITY_PAUSED else UsageEvents.Event.MOVE_TO_BACKGROUND
    }
}
