package com.healthjournal.presentation.screen.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {
    private val userSettingsRepository = (application as HealthJournalApp).container.userSettingsRepository

    val settings = userSettingsRepository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun completeOnboarding(userName: String, aiConsent: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.updateUserSettings(
                settings.value.copy(
                    userName = userName,
                    aiConsentGiven = aiConsent,
                    onboardingCompleted = true
                )
            )
        }
    }
}
