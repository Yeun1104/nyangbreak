package com.example.nb_frontend.backend.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import com.example.nb_frontend.backend.service.MonitorController
import com.example.nb_frontend.backend.util.PermissionUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PermissionUiState(
    val usageGranted: Boolean = false,
    val overlayGranted: Boolean = false,
    val notificationGranted: Boolean = false,
) {
    val allGranted: Boolean get() = usageGranted && overlayGranted && notificationGranted
}

class PermissionViewModel(app: Application) : AndroidViewModel(app) {
    private val _uiState = MutableStateFlow(read())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    val notificationPermission: String? = PermissionUtil.notificationPermission

    fun refresh() {
        _uiState.value = read()
        MonitorController.ensureRunning(getApplication())
    }

    fun usageAccessIntent(): Intent = PermissionUtil.usageAccessSettingsIntent()
    fun overlayIntent(): Intent = PermissionUtil.overlaySettingsIntent(getApplication())

    private fun read(): PermissionUiState {
        val ctx = getApplication<Application>()
        return PermissionUiState(
            usageGranted = PermissionUtil.hasUsageAccess(ctx),
            overlayGranted = PermissionUtil.canDrawOverlays(ctx),
            notificationGranted = PermissionUtil.hasNotificationPermission(ctx),
        )
    }
}
