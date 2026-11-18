package com.example.nb_frontend.backend.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_limit")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val limitMinutes: Int,
    val accumulatedMillis: Long = 0,
    val enabled: Boolean = true,
)

@Entity(tableName = "usage_session", indices = [Index("date"), Index("packageName")])
data class UsageSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    val packageName: String,
    val date: String,
    val startedAt: Long,
    val endedAt: Long,
    val durationMillis: Long,
)

enum class BreakEndReason { ONGOING, TIMEOUT, TAPPED, CANCELLED }

@Entity(tableName = "break_event", indices = [Index("date"), Index("packageName")])
data class BreakEvent(
    @PrimaryKey(autoGenerate = true) val breakId: Long = 0,
    val packageName: String,
    val date: String,
    val startedAt: Long,
    val endedAt: Long? = null,
    val endReason: BreakEndReason = BreakEndReason.ONGOING,
)
