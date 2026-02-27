package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val settingsRepo = container.userSettingsRepository
    private val aiService = container.aiService
    private val registry = container.aiProviderRegistry

    val aiSettings: StateFlow<AiSettings> = settingsRepo.getUserSettings()
        .map { it.aiSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiSettings())

    val providers = registry.getAll()

    private val _validationMessage = MutableStateFlow<String?>(null)
    val validationMessage = _validationMessage.asStateFlow()

    fun selectProvider(providerId: String) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(selectedProviderId = providerId))
        }
    }

    fun toggleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(enabled = enabled))
        }
    }

    fun togglePrivacyRedact(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(privacyRedactEnabled = enabled))
        }
    }

    fun updateClaudeConfig(config: ClaudeConfig) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(claudeConfig = config))
        }
    }

    fun updateOpenAiConfig(config: OpenAiConfig) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(openAiConfig = config))
        }
    }

    fun updateGeminiNanoConfig(config: GeminiNanoConfig) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(geminiNanoConfig = config))
        }
    }

    fun updateLocalConfig(config: LocalAiConfig) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(localAiConfig = config))
        }
    }

    fun validateCurrentProvider() {
        val result = aiService.validateActiveProvider(aiSettings.value)
        _validationMessage.value = if (result.valid) null else result.errorMessage
    }
}
