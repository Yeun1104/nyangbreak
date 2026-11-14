package com.example.nb_frontend.backend.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.nb_frontend.MainActivity
import com.example.nb_frontend.backend.NyangBreakApp
import com.example.nb_frontend.R
import com.example.nb_frontend.backend.block.BlockOverlay
import com.example.nb_frontend.backend.block.EngineStateStore
import com.example.nb_frontend.backend.block.GatekeeperEngine
import com.example.nb_frontend.backend.block.GatekeeperEngine.EndReason
import com.example.nb_frontend.backend.block.GatekeeperEngine.Event
import com.example.nb_frontend.backend.data.db.BreakEndReason
import com.example.nb_frontend.backend.data.repository.NyangRepository
import com.example.nb_frontend.backend.data.settings.SettingsRepository
import com.example.nb_frontend.backend.tracking.ForegroundAppDetector
import com.example.nb_frontend.backend.util.PermissionUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** 1초마다 현재 앱을 확인하고 상태 전이를 처리하는 포그라운드 서비스 */
class MonitorService : LifecycleService() {

    private lateinit var repo: NyangRepository
    private lateinit var settings: SettingsRepository
    private lateinit var detector: ForegroundAppDetector
    private lateinit var overlay: BlockOverlay
    private lateinit var stateStore: EngineStateStore
    private var engine: GatekeeperEngine? = null

    private var loopJob: Job? = null
    private var limits: Map<String, Long> = emptyMap()
    private var tickCount = 0

    override fun onCreate() {
        super.onCreate()
        repo = NyangRepository.get(this)
        settings = SettingsRepository.get(this)
        detector = ForegroundAppDetector(this)
        overlay = BlockOverlay(this)
        stateStore = EngineStateStore(this)
        instance = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startAsForeground()

        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (loopJob?.isActive != true) {
            loopJob = lifecycleScope.launch {
                restore()
                while (isActive) {
                    runCatching { tick() }.onFailure { Log.e(TAG, "tick 실패", it) }
                    delay(TICK_MS)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        instance = null
        overlay.hide()
        engine?.let { e ->
            val now = System.currentTimeMillis()
            runBlocking {
                e.closeOpenSession(now)?.let { handle(it) }
                saveAccumulated()
            }
            stateStore.saveBreaks(e.state.breaks)
        }
        MonitorStatus.update(LiveStatus(serviceRunning = false))
        super.onDestroy()
    }

    private suspend fun restore() {
        val apps = repo.getEnabledAppLimits()
        val breaks = stateStore.loadBreaks()
        engine = GatekeeperEngine(
            GatekeeperEngine.State(
                accumulated = apps.associate { it.packageName to it.accumulatedMillis },
                breaks = breaks,
            )
        )
        repo.closeDanglingBreaksExcept(System.currentTimeMillis(), breaks.values.mapNotNull { it.breakEventId })
    }

    private suspend fun tick() {
        val e = engine ?: return
        val now = System.currentTimeMillis()
        val s = settings.current

        if (tickCount % RELOAD_LIMITS_EVERY == 0) {
            limits = repo.getEnabledAppLimits().associate { it.packageName to it.limitMinutes * 60_000L }
        }

        val foreground = detector.detect(now)
        val result = e.tick(
            now = now,
            foregroundPackage = foreground,
            limitsMillis = limits,
            breakMillis = s.breakSeconds * 1000L,
            paused = s.paused,
        )
        for (event in result.events) {
            Log.d(TAG, "상태 전이: $event")
            handle(event)
        }

        val pkg = result.overlayPackage
        if (pkg != null) {
            val wasShowing = overlay.showingPackage == pkg
            when {
                !overlay.show(pkg, s.cat, s.tapToDismissEnabled) { onRelease(pkg) } ->
                    Log.w(TAG, "다른 앱 위에 표시 권한이 없어 고양이를 띄우지 못함")
                !wasShowing -> Log.d(TAG, "고양이 표시: $pkg")
            }
            overlay.updateCountdown(result.breakRemainingMillis)
        } else {
            overlay.hide()
        }

        publishStatus(e, now, foreground)

        if (++tickCount % SAVE_EVERY_TICKS == 0) {
            saveAccumulated()
            updateNotification()
        }
        if (result.events.isNotEmpty()) {
            stateStore.saveBreaks(e.state.breaks)
            updateNotification()
        }
    }

    private suspend fun handle(event: Event) {
        when (event) {
            is Event.SessionEnded ->
                repo.saveSession(event.packageName, event.startedAt, event.endedAt, event.durationMillis)
            is Event.BreakStarted -> {
                repo.saveAccumulated(event.packageName, engine?.accumulatedOf(event.packageName) ?: 0)
                engine?.attachBreakEventId(event.packageName, repo.startBreak(event.packageName, event.at))
            }
            is Event.BreakEnded -> {
                event.breakEventId?.let { repo.finishBreak(it, event.at, event.reason.toDb()) }
                repo.saveAccumulated(event.packageName, 0)
            }
        }
    }

    private fun onRelease(pkg: String) {
        if (!settings.current.tapToDismissEnabled) return
        val e = engine ?: return
        val event = e.tapDismiss(pkg, System.currentTimeMillis()) ?: return
        overlay.hide()
        lifecycleScope.launch {
            handle(event)
            stateStore.saveBreaks(e.state.breaks)
        }
    }

    private suspend fun saveAccumulated() {
        val e = engine ?: return
        for (pkg in limits.keys) repo.saveAccumulated(pkg, e.accumulatedOf(pkg))
    }

    private fun publishStatus(e: GatekeeperEngine, now: Long, foreground: String?) {
        MonitorStatus.update(
            LiveStatus(
                serviceRunning = true,
                foregroundPackage = foreground,
                accumulated = limits.keys.associateWith { e.accumulatedOf(it) },
                breakRemaining = e.state.breaks.mapValues { (it.value.until - now).coerceAtLeast(0) },
                openSessionMillis = e.state.session?.durationMillis ?: 0,
            )
        )
    }

    private fun EndReason.toDb(): BreakEndReason = when (this) {
        EndReason.TIMEOUT -> BreakEndReason.TIMEOUT
        EndReason.TAPPED -> BreakEndReason.TAPPED
        EndReason.CANCELLED -> BreakEndReason.CANCELLED
    }

    private fun buildNotification(): Notification {
        val s = settings.current
        val text = when {
            !PermissionUtil.hasUsageAccess(this) -> "사용 정보 접근 권한이 필요해요"
            !PermissionUtil.canDrawOverlays(this) -> "다른 앱 위에 표시 권한이 필요해요"
            s.paused -> "일시정지 중이에요"
            engine?.isAnyBreakActive == true -> "고양이와 쉬는 중이에요"
            else -> "앱 ${limits.size}개를 지켜보는 중"
        }
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, NyangBreakApp.CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_paw)
            .setContentTitle("냥브레이크")
            .setContentText(text)
            .setContentIntent(open)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startAsForeground() {
        val n = buildNotification()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, n)
        }
    }

    private fun updateNotification() {
        if (!PermissionUtil.hasNotificationPermission(this)) return
        try {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification())
        } catch (_: SecurityException) {
        }
    }

    companion object {
        private const val TAG = "MonitorService"
        private const val NOTIFICATION_ID = 1001
        private const val TICK_MS = 1_000L
        private const val SAVE_EVERY_TICKS = 10
        private const val RELOAD_LIMITS_EVERY = 3

        const val ACTION_STOP = "com.example.nb_frontend.action.STOP_MONITOR"

        @Volatile internal var instance: MonitorService? = null
            private set
    }
}
