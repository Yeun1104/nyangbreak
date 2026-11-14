package com.example.nb_frontend.backend.data.settings

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.edit
import com.example.nb_frontend.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CatCharacter(@DrawableRes val imageRes: Int) {
    TABBY(R.drawable.cat_tabby),
    GRAY_WHITE(R.drawable.cat_gray_white),
    TUXEDO(R.drawable.cat_tuxedo);

    companion object {
        fun fromIndex(index: Int): CatCharacter = entries.getOrElse(index) { TABBY }
    }
}

data class AppSettings(
    val breakSeconds: Int = 30,
    val tapToDismissEnabled: Boolean = true,
    val catIndex: Int = 0,
    val paused: Boolean = false,
    val defaultLimitMinutes: Int = 60,
) {
    val cat: CatCharacter get() = CatCharacter.fromIndex(catIndex)
}

class SettingsRepository private constructor(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    val current: AppSettings get() = _settings.value

    fun setBreakSeconds(seconds: Int) = update { it.copy(breakSeconds = seconds.coerceIn(MIN_BREAK_SEC, MAX_BREAK_SEC)) }
    fun setTapToDismissEnabled(enabled: Boolean) = update { it.copy(tapToDismissEnabled = enabled) }
    fun setCatIndex(index: Int) = update { it.copy(catIndex = index.coerceIn(0, CatCharacter.entries.lastIndex)) }
    fun setDefaultLimitMinutes(minutes: Int) =
        update { it.copy(defaultLimitMinutes = minutes.coerceIn(MIN_LIMIT_MIN, MAX_LIMIT_MIN)) }

    internal fun setPaused(paused: Boolean) = update { it.copy(paused = paused) }

    private fun update(change: (AppSettings) -> AppSettings) {
        val next = change(_settings.value)
        _settings.value = next
        prefs.edit {
            putInt(K_BREAK, next.breakSeconds)
            putBoolean(K_TAP, next.tapToDismissEnabled)
            putInt(K_CAT, next.catIndex)
            putBoolean(K_PAUSED, next.paused)
            putInt(K_DEFAULT_LIMIT, next.defaultLimitMinutes)
        }
    }

    private fun load(): AppSettings {
        val d = AppSettings()
        return AppSettings(
            breakSeconds = prefs.getInt(K_BREAK, d.breakSeconds),
            tapToDismissEnabled = prefs.getBoolean(K_TAP, d.tapToDismissEnabled),
            catIndex = prefs.getInt(K_CAT, d.catIndex),
            paused = prefs.getBoolean(K_PAUSED, d.paused),
            defaultLimitMinutes = prefs.getInt(K_DEFAULT_LIMIT, d.defaultLimitMinutes),
        )
    }

    companion object {
        const val MIN_BREAK_SEC = 5
        const val MAX_BREAK_SEC = 300
        const val MIN_LIMIT_MIN = 1
        const val MAX_LIMIT_MIN = 180

        private const val K_BREAK = "break_seconds"
        private const val K_TAP = "tap_to_dismiss"
        private const val K_CAT = "cat_index"
        private const val K_PAUSED = "paused"
        private const val K_DEFAULT_LIMIT = "default_limit_minutes"

        @Volatile private var instance: SettingsRepository? = null
        fun get(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}
