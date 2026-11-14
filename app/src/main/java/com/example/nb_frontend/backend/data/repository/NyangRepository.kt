package com.example.nb_frontend.backend.data.repository

import android.content.Context
import com.example.nb_frontend.backend.data.db.AppDatabase
import com.example.nb_frontend.backend.data.db.AppLimit
import com.example.nb_frontend.backend.data.db.BreakEndReason
import com.example.nb_frontend.backend.data.db.BreakEvent
import com.example.nb_frontend.backend.data.db.UsageByApp
import com.example.nb_frontend.backend.data.db.UsageByDate
import com.example.nb_frontend.backend.data.db.UsageSession
import com.example.nb_frontend.backend.util.DateUtil
import kotlinx.coroutines.flow.Flow

class NyangRepository private constructor(db: AppDatabase) {
    private val limitDao = db.appLimitDao()
    private val sessionDao = db.usageSessionDao()
    private val breakDao = db.breakEventDao()

    fun observeAppLimits(): Flow<List<AppLimit>> = limitDao.observeAll()
    fun observeEnabledAppLimits(): Flow<List<AppLimit>> = limitDao.observeEnabled()
    suspend fun getEnabledAppLimits(): List<AppLimit> = limitDao.getEnabled()

    suspend fun setTracked(packageName: String, appLabel: String, enabled: Boolean, defaultLimitMinutes: Int) {
        val existing = limitDao.get(packageName)
        if (existing == null) {
            if (enabled) limitDao.upsert(AppLimit(packageName, appLabel, defaultLimitMinutes))
        } else {
            limitDao.setEnabled(packageName, enabled)
        }
    }

    suspend fun setLimitMinutes(packageName: String, minutes: Int) = limitDao.setLimit(packageName, minutes)
    suspend fun saveAccumulated(packageName: String, millis: Long) = limitDao.setAccumulated(packageName, millis)

    suspend fun saveSession(packageName: String, startedAt: Long, endedAt: Long, durationMillis: Long) {
        if (durationMillis <= 0) return
        sessionDao.insert(UsageSession(packageName = packageName, date = DateUtil.dateKey(startedAt),
            startedAt = startedAt, endedAt = endedAt, durationMillis = durationMillis))
    }
    fun observeTotalOn(date: String): Flow<Long> = sessionDao.observeTotalOn(date)
    fun observeDailyTotals(from: String, to: String): Flow<List<UsageByDate>> = sessionDao.observeTotalsByDate(from, to)
    fun observeTotalsByApp(from: String, to: String): Flow<List<UsageByApp>> = sessionDao.observeTotalsByApp(from, to)

    suspend fun startBreak(packageName: String, startedAt: Long): Long =
        breakDao.insert(BreakEvent(packageName = packageName, date = DateUtil.dateKey(startedAt), startedAt = startedAt))
    suspend fun finishBreak(id: Long, endedAt: Long, reason: BreakEndReason) = breakDao.finish(id, endedAt, reason)
    suspend fun closeDanglingBreaksExcept(now: Long, keepIds: List<Long>) = breakDao.closeDanglingExcept(now, keepIds)
    fun observeBreakCountOn(date: String): Flow<Int> = breakDao.observeCountOn(date)

    suspend fun deleteOlderThan(beforeDate: String): Int =
        sessionDao.deleteBefore(beforeDate) + breakDao.deleteBefore(beforeDate)

    companion object {
        @Volatile private var instance: NyangRepository? = null
        fun get(context: Context): NyangRepository =
            instance ?: synchronized(this) {
                instance ?: NyangRepository(AppDatabase.get(context)).also { instance = it }
            }
    }
}
