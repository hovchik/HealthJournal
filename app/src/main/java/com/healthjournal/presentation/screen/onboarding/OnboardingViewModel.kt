package com.healthjournal.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.domain.model.UserSettings
import com.healthjournal.domain.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

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
