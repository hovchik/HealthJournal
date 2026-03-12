package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.data.ai.ModelCatalog
import com.healthjournal.domain.model.ai.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiSettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val settingsRepo = container.userSettingsRepository
    private val aiService = container.aiService
    private val registry = container.aiProviderRegistry
    private val aiPreferences = container.aiPreferences
    private val localModelManager = container.localModelManager
    private val modelInstaller = container.modelInstaller
    private val deviceDetector = container.deviceCapabilityDetector
    private val compatValidator = container.modelCompatibilityValidator

    val aiSettings: StateFlow<AiSettings> = settingsRepo.getUserSettings()
        .map { it.aiSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiSettings())

    val providers = registry.getAll()

    val executionMode: StateFlow<AiExecutionMode> = aiPreferences.observeSelectedMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiExecutionMode.AUTO)

    val installedModels: StateFlow<List<LocalAiModel>> = localModelManager.observeInstalledModels()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeModel: StateFlow<LocalAiModel?> = localModelManager.observeActiveModel()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val installProgress = modelInstaller.installProgress

    val catalogModels: List<LocalAiModel> = ModelCatalog.availableModels

    private val _deviceCapability = MutableStateFlow<DeviceCapabilityResult?>(null)
    val deviceCapability = _deviceCapability.asStateFlow()

    private val _validationMessage = MutableStateFlow<String?>(null)
    val validationMessage = _validationMessage.asStateFlow()

    private val _validationSuccess = MutableStateFlow<Boolean?>(null)
    val validationSuccess = _validationSuccess.asStateFlow()

    private val _scanResult = MutableStateFlow<Int?>(null)
    val scanResult = _scanResult.asStateFlow()

    init {
        viewModelScope.launch {
            _deviceCapability.value = deviceDetector.detect()
        }
    }

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

    fun updateLocalConfig(config: LocalAiConfig) {
        viewModelScope.launch {
            settingsRepo.setAiSettings(aiSettings.value.copy(localAiConfig = config))
        }
    }

    fun setExecutionMode(mode: AiExecutionMode) {
        viewModelScope.launch {
            aiPreferences.setSelectedMode(mode)
        }
    }

    fun downloadModel(model: LocalAiModel) {
        val url = model.downloadUrl ?: return
        viewModelScope.launch {
            modelInstaller.downloadModel(model, url)
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            localModelManager.deleteModel(modelId)
        }
    }

    fun setActiveModel(modelId: String) {
        viewModelScope.launch {
            localModelManager.setActiveModel(modelId)
        }
    }

    fun scanForModels() {
        viewModelScope.launch {
            val found = modelInstaller.scanForModels()
            _scanResult.value = found
        }
    }

    fun getCompatibilityReport(model: LocalAiModel): CompatibilityReport {
        return compatValidator.validate(model, _deviceCapability.value)
    }

    fun validateCurrentProvider() {
        viewModelScope.launch {
            val currentSettings = settingsRepo.getUserSettings().first().aiSettings
            val result = aiService.validateActiveProvider(currentSettings)
            _validationSuccess.value = result.valid
            _validationMessage.value = result.errorMessage
        }
    }
}
