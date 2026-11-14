package com.example.nb_frontend.backend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nb_frontend.backend.data.repository.InstalledApp
import com.example.nb_frontend.backend.data.repository.InstalledAppsRepository
import com.example.nb_frontend.backend.data.repository.NyangRepository
import com.example.nb_frontend.backend.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TrackedAppsUiState(
    val apps: List<InstalledApp> = emptyList(),
    val trackedPackages: Set<String> = emptySet(),
    val limitMinutes: Map<String, Int> = emptyMap(),
    val loading: Boolean = true,
)

class TrackedAppsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = NyangRepository.get(app)
    private val settings = SettingsRepository.get(app)
    private val installed = MutableStateFlow<List<InstalledApp>?>(null)

    init {
        viewModelScope.launch { installed.value = InstalledAppsRepository(app).getLaunchableApps() }
    }

    val uiState: StateFlow<TrackedAppsUiState> = combine(installed, repo.observeAppLimits()) { apps, limits ->
        TrackedAppsUiState(
            apps = apps.orEmpty(),
            trackedPackages = limits.filter { it.enabled }.map { it.packageName }.toSet(),
            limitMinutes = limits.associate { it.packageName to it.limitMinutes },
            loading = apps == null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrackedAppsUiState())

    fun setTracked(packageName: String, enabled: Boolean) {
        val label = installed.value?.firstOrNull { it.packageName == packageName }?.name ?: packageName
        viewModelScope.launch { repo.setTracked(packageName, label, enabled, settings.current.defaultLimitMinutes) }
    }

    fun setAllTracked(enabled: Boolean) {
        val apps = installed.value.orEmpty()
        viewModelScope.launch {
            apps.forEach { repo.setTracked(it.packageName, it.name, enabled, settings.current.defaultLimitMinutes) }
        }
    }

    fun setLimitMinutes(packageName: String, minutes: Int) {
        viewModelScope.launch {
            repo.setLimitMinutes(packageName, minutes.coerceIn(SettingsRepository.MIN_LIMIT_MIN, SettingsRepository.MAX_LIMIT_MIN))
        }
    }
}
