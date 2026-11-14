package com.example.nb_frontend.backend.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.nb_frontend.backend.data.settings.AppSettings
import com.example.nb_frontend.backend.data.settings.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = SettingsRepository.get(app)
    val settings: StateFlow<AppSettings> = repo.settings

    fun setBreakSeconds(seconds: Int) = repo.setBreakSeconds(seconds)
    fun setTapToDismissEnabled(enabled: Boolean) = repo.setTapToDismissEnabled(enabled)
    fun setCatIndex(index: Int) = repo.setCatIndex(index)
    fun setDefaultLimitMinutes(minutes: Int) = repo.setDefaultLimitMinutes(minutes)
}
