package com.example.nb_frontend.backend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nb_frontend.backend.data.repository.NyangRepository
import com.example.nb_frontend.backend.data.settings.SettingsRepository
import com.example.nb_frontend.backend.service.MonitorController
import com.example.nb_frontend.backend.service.MonitorStatus
import com.example.nb_frontend.backend.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class TrackedAppStatus(
    val packageName: String,
    val name: String,
    val limitMinutes: Int,
    val usedMinutes: Int,
    val remainingMinutes: Int,
    val onBreak: Boolean,
    val breakRemainingSeconds: Int,
)

data class HomeUiState(
    val apps: List<TrackedAppStatus> = emptyList(),
    val todayHours: Int = 0,
    val todayMinutes: Int = 0,
    val catCount: Int = 0,
    val isPaused: Boolean = false,
    val isMonitoring: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = NyangRepository.get(app)
    private val settings = SettingsRepository.get(app)

    private val todayTotal = DateUtil.todayFlow().flatMapLatest { repo.observeTotalOn(it) }
    private val todayBreaks = DateUtil.todayFlow().flatMapLatest { repo.observeBreakCountOn(it) }

    val uiState: StateFlow<HomeUiState> = combine(
        repo.observeEnabledAppLimits(), MonitorStatus.live, settings.settings, todayTotal, todayBreaks,
    ) { limits, live, s, totalMs, breaks ->
        val totalMin = ((totalMs + live.openSessionMillis) / 60_000).toInt()
        HomeUiState(
            apps = limits.map { a ->
                val used = live.accumulated[a.packageName] ?: a.accumulatedMillis
                val remainMs = (a.limitMinutes * 60_000L - used).coerceAtLeast(0)
                val breakMs = live.breakRemaining[a.packageName]
                TrackedAppStatus(
                    packageName = a.packageName,
                    name = a.appLabel,
                    limitMinutes = a.limitMinutes,
                    usedMinutes = (used / 60_000).toInt(),
                    remainingMinutes = ((remainMs + 59_999) / 60_000).toInt(),
                    onBreak = breakMs != null,
                    breakRemainingSeconds = ((breakMs ?: 0) / 1000).toInt(),
                )
            },
            todayHours = totalMin / 60,
            todayMinutes = totalMin % 60,
            catCount = breaks,
            isPaused = s.paused,
            isMonitoring = live.serviceRunning,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun togglePause(): Boolean = MonitorController.setPaused(getApplication(), !settings.current.paused)
}
