package com.healthjournal.presentation.screen.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val userSettingsRepository = (application as HealthJournalApp).container.userSettingsRepository

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded = _isLoaded.asStateFlow()

    val settings = userSettingsRepository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    init {
        viewModelScope.launch {
            userSettingsRepository.getUserSettings().first()
            _isLoaded.value = true
        }
    }

    fun completeOnboarding(userName: String, aiConsent: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateUserSettings(
                settings.value.copy(
                    userName = userName,
                    aiConsentGiven = aiConsent,
                    onboardingCompleted = true,
                    termsAcceptedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
