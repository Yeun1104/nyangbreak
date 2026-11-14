package com.example.nb_frontend.backend.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.nb_frontend.backend.data.settings.SettingsRepository
import com.example.nb_frontend.backend.util.PermissionUtil

object MonitorController {
    private const val TAG = "MonitorController"

    val isRunning: Boolean get() = MonitorService.instance != null
    val isAnyBreakActive: Boolean get() = MonitorStatus.live.value.breakRemaining.isNotEmpty()

    fun ensureRunning(context: Context): Boolean {
        if (isRunning) return true
        if (!PermissionUtil.hasRequired(context)) return false
        return try {
            context.startForegroundService(Intent(context, MonitorService::class.java))
            true
        } catch (e: IllegalStateException) {
            Log.w(TAG, "감시 서비스를 시작하지 못함", e)
            false
        }
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, MonitorService::class.java))
    }

    fun setPaused(context: Context, paused: Boolean): Boolean {
        val settings = SettingsRepository.get(context)
        if (paused && isAnyBreakActive) return settings.current.paused
        settings.setPaused(paused)
        return paused
    }
}
