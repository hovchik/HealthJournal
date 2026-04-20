package com.hovchik.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.util.LocaleManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanguageSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val userSettingsRepository = (application as HealthJournalApp).container.userSettingsRepository

    val currentLanguage = userSettingsRepository.getUserSettings()
        .map { it.languageMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    fun setLanguage(languageMode: String) {
        viewModelScope.launch {
            userSettingsRepository.setLanguageMode(languageMode)
            LocaleManager.applyLocale(languageMode)
        }
    }
}
