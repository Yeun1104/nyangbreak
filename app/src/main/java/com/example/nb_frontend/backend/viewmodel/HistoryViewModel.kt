package com.example.nb_frontend.backend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nb_frontend.backend.data.repository.NyangRepository
import com.example.nb_frontend.backend.util.DateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class DayUsageData(val day: String, val hours: Float, val isToday: Boolean)

data class AppUsageData(val name: String, val minutes: Int, val packageName: String)

data class HistoryUiState(
    val todayMinutes: Int = 0,
    val blockCount: Int = 0,
    val weekly: List<DayUsageData> = emptyList(),
    val apps: List<AppUsageData> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = NyangRepository.get(app)

    val uiState: StateFlow<HistoryUiState> = DateUtil.todayFlow().flatMapLatest { today ->
        combine(
            repo.observeTotalOn(today),
            repo.observeBreakCountOn(today),
            repo.observeDailyTotals(DateUtil.daysAgo(6), today),
            repo.observeTotalsByApp(today, today),
        ) { totalMs, breaks, daily, byApp ->
            val byDate = daily.associate { it.date to it.totalMillis }
            val todayDate = DateUtil.parse(today)
            HistoryUiState(
                todayMinutes = (totalMs / 60_000).toInt(),
                blockCount = breaks,
                weekly = (6 downTo 0).map { ago ->
                    val d = todayDate.minusDays(ago.toLong())
                    DayUsageData(DateUtil.koreanDayOfWeek(d), (byDate[d.toString()] ?: 0L) / 3_600_000f, ago == 0)
                },
                apps = byApp.map { AppUsageData(it.appLabel ?: it.packageName, (it.totalMillis / 60_000).toInt(), it.packageName) },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState())
}
