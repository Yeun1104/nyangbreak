package com.example.nb_frontend.backend

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import com.example.nb_frontend.backend.service.MonitorController
import com.example.nb_frontend.backend.work.WorkScheduler
import com.example.nb_frontend.backend.block.BreakScreenRegistry
import com.example.nb_frontend.frontend.ui.overlay.BreakScreen

class NyangBreakApp : Application() {
    override fun onCreate() {
        super.onCreate()
        BreakScreenRegistry.register { state, release ->
            BreakScreen(state = state, onRelease = release)
        }
        createNotificationChannels()
        WorkScheduler.scheduleAll(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                MonitorController.ensureRunning(activity)
            }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_MONITOR, "사용 시간 감시", NotificationManager.IMPORTANCE_LOW)
                .apply { setShowBadge(false) }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ALERT, "고양이 등장 미리 알림", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    companion object {
        const val CHANNEL_MONITOR = "monitor"
        const val CHANNEL_ALERT = "pre_alert"
    }
}
