package com.example.nb_frontend.backend.block

/** 앱별 상태 전이 엔진: 감시 중 → 휴식 중 → 해제 */
class GatekeeperEngine(initialState: State = State()) {

    data class OpenSession(val packageName: String, val startedAt: Long, val durationMillis: Long = 0)

    data class ActiveBreak(val until: Long, val breakEventId: Long? = null)

    data class State(
        val accumulated: Map<String, Long> = emptyMap(),
        val breaks: Map<String, ActiveBreak> = emptyMap(),
        val session: OpenSession? = null,
        val lastTickAt: Long? = null,
    )

    enum class EndReason { TIMEOUT, TAPPED, CANCELLED }

    sealed interface Event {
        data class SessionEnded(val packageName: String, val startedAt: Long, val endedAt: Long, val durationMillis: Long) : Event
        data class BreakStarted(val packageName: String, val at: Long, val until: Long) : Event
        data class BreakEnded(val packageName: String, val at: Long, val reason: EndReason, val breakEventId: Long?) : Event
    }

    data class TickResult(
        val overlayPackage: String? = null,
        val breakRemainingMillis: Long = 0,
        val events: List<Event> = emptyList(),
    )

    var state: State = initialState
        private set

    val isAnyBreakActive: Boolean get() = state.breaks.isNotEmpty()

    fun isOnBreak(pkg: String): Boolean = pkg in state.breaks

    fun accumulatedOf(pkg: String): Long = state.accumulated[pkg] ?: 0L

    fun attachBreakEventId(pkg: String, id: Long) {
        val b = state.breaks[pkg] ?: return
        state = state.copy(breaks = state.breaks + (pkg to b.copy(breakEventId = id)))
    }

    fun tick(
        now: Long,
        foregroundPackage: String?,
        limitsMillis: Map<String, Long>,
        breakMillis: Long,
        paused: Boolean,
    ): TickResult {
        val elapsed = state.lastTickAt?.let { (now - it).coerceIn(0, MAX_TICK_GAP_MS) } ?: 0L
        state = state.copy(lastTickAt = now)
        val events = mutableListOf<Event>()

        for ((pkg, b) in state.breaks) {
            when {
                pkg !in limitsMillis -> events += release(pkg, now, EndReason.CANCELLED)
                now >= b.until -> events += release(pkg, now, EndReason.TIMEOUT)
            }
        }

        val counted = foregroundPackage?.takeIf { !paused && it in limitsMillis && it !in state.breaks }

        val open = state.session
        if (open != null && open.packageName != counted) {
            events += closeSession(now)
        }
        if (counted != null && state.session == null) {
            state = state.copy(session = OpenSession(counted, now))
        }

        if (counted != null) {
            val acc = accumulatedOf(counted) + elapsed
            state = state.copy(
                accumulated = state.accumulated + (counted to acc),
                session = state.session?.let { it.copy(durationMillis = it.durationMillis + elapsed) },
            )
            if (acc >= limitsMillis.getValue(counted)) {
                events += closeSession(now)
                val until = now + breakMillis
                state = state.copy(breaks = state.breaks + (counted to ActiveBreak(until)))
                events += Event.BreakStarted(counted, now, until)
            }
        }

        val overlayPkg = foregroundPackage?.takeIf { it in state.breaks }
        return TickResult(
            overlayPackage = overlayPkg,
            breakRemainingMillis = overlayPkg?.let { (state.breaks.getValue(it).until - now).coerceAtLeast(0) } ?: 0,
            events = events,
        )
    }

    fun tapDismiss(pkg: String, now: Long): Event? =
        if (pkg in state.breaks) release(pkg, now, EndReason.TAPPED) else null

    fun closeOpenSession(now: Long): Event? = if (state.session != null) closeSession(now) else null

    private fun closeSession(now: Long): Event {
        val s = state.session!!
        state = state.copy(session = null)
        return Event.SessionEnded(s.packageName, s.startedAt, now, s.durationMillis)
    }

    private fun release(pkg: String, now: Long, reason: EndReason): Event {
        val b = state.breaks.getValue(pkg)
        state = state.copy(
            breaks = state.breaks - pkg,
            accumulated = state.accumulated + (pkg to 0L),
        )
        return Event.BreakEnded(pkg, now, reason, b.breakEventId)
    }

    companion object {
        const val MAX_TICK_GAP_MS = 5_000L
    }
}
