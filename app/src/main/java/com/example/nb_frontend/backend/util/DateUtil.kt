package com.example.nb_frontend.backend.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtil {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun dateKey(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(formatter)

    fun today(): String = LocalDate.now().format(formatter)

    fun daysAgo(days: Long): String = LocalDate.now().minusDays(days).format(formatter)

    fun parse(key: String): LocalDate = LocalDate.parse(key, formatter)

    fun koreanDayOfWeek(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

    fun todayFlow(): Flow<String> = flow {
        while (true) {
            emit(today())
            delay(60_000)
        }
    }.distinctUntilChanged()

    fun millisUntilNextMidnight(now: Long = System.currentTimeMillis(), zone: ZoneId = ZoneId.systemDefault()): Long {
        val next = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
            .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return next - now
    }
}
