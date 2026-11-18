package com.example.nb_frontend.backend.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nb_frontend.backend.data.repository.NyangRepository
import com.example.nb_frontend.backend.util.DateUtil

class DailyCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        NyangRepository.get(applicationContext).deleteOlderThan(DateUtil.daysAgo(KEEP_DAYS))
        return Result.success()
    }

    companion object {
        const val KEEP_DAYS = 90L
    }
}
