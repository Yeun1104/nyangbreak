package com.example.nb_frontend.backend.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLimitDao {
    @Query("SELECT * FROM app_limit ORDER BY appLabel")
    fun observeAll(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limit WHERE enabled = 1 ORDER BY appLabel")
    fun observeEnabled(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limit WHERE enabled = 1")
    suspend fun getEnabled(): List<AppLimit>

    @Query("SELECT * FROM app_limit WHERE packageName = :pkg")
    suspend fun get(pkg: String): AppLimit?

    @Upsert
    suspend fun upsert(limit: AppLimit)

    @Query("UPDATE app_limit SET enabled = :enabled WHERE packageName = :pkg")
    suspend fun setEnabled(pkg: String, enabled: Boolean)

    @Query("UPDATE app_limit SET limitMinutes = :minutes WHERE packageName = :pkg")
    suspend fun setLimit(pkg: String, minutes: Int)

    @Query("UPDATE app_limit SET accumulatedMillis = :millis WHERE packageName = :pkg")
    suspend fun setAccumulated(pkg: String, millis: Long)
}

data class UsageByDate(val date: String, val totalMillis: Long)
data class UsageByApp(val packageName: String, val appLabel: String?, val totalMillis: Long)

@Dao
interface UsageSessionDao {
    @Insert
    suspend fun insert(session: UsageSession): Long

    @Query("SELECT COALESCE(SUM(durationMillis), 0) FROM usage_session WHERE date = :date")
    fun observeTotalOn(date: String): Flow<Long>

    @Query(
        """
        SELECT date, SUM(durationMillis) AS totalMillis FROM usage_session
        WHERE date BETWEEN :from AND :to GROUP BY date ORDER BY date
        """
    )
    fun observeTotalsByDate(from: String, to: String): Flow<List<UsageByDate>>

    @Query(
        """
        SELECT s.packageName AS packageName, a.appLabel AS appLabel, SUM(s.durationMillis) AS totalMillis
        FROM usage_session s LEFT JOIN app_limit a ON a.packageName = s.packageName
        WHERE s.date BETWEEN :from AND :to
        GROUP BY s.packageName ORDER BY totalMillis DESC
        """
    )
    fun observeTotalsByApp(from: String, to: String): Flow<List<UsageByApp>>

    @Query("DELETE FROM usage_session WHERE date < :beforeDate")
    suspend fun deleteBefore(beforeDate: String): Int
}

@Dao
interface BreakEventDao {
    @Insert
    suspend fun insert(event: BreakEvent): Long

    @Query("UPDATE break_event SET endedAt = :endedAt, endReason = :reason WHERE breakId = :id")
    suspend fun finish(id: Long, endedAt: Long, reason: BreakEndReason)

    @Query("UPDATE break_event SET endedAt = :now, endReason = 'TIMEOUT' WHERE endReason = 'ONGOING' AND breakId NOT IN (:keepIds)")
    suspend fun closeDanglingExcept(now: Long, keepIds: List<Long>)

    @Query("SELECT COUNT(*) FROM break_event WHERE date = :date")
    fun observeCountOn(date: String): Flow<Int>

    @Query("DELETE FROM break_event WHERE date < :beforeDate")
    suspend fun deleteBefore(beforeDate: String): Int
}
