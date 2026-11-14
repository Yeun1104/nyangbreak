package com.example.nb_frontend.backend.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveStatus(
    val serviceRunning: Boolean = false,
    val foregroundPackage: String? = null,
    val accumulated: Map<String, Long> = emptyMap(),
    val breakRemaining: Map<String, Long> = emptyMap(),
    val openSessionMillis: Long = 0,
)

object MonitorStatus {
    private val _live = MutableStateFlow(LiveStatus())
    val live: StateFlow<LiveStatus> = _live.asStateFlow()

    internal fun update(status: LiveStatus) {
        _live.value = status
    }
}
