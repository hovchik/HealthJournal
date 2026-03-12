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

    val scanProgress = modelInstaller.scanProgress

    val catalogModels: List<LocalAiModel> = ModelCatalog.availableModels

    private val _deviceCapability = MutableStateFlow<DeviceCapabilityResult?>(null)
    val deviceCapability = _deviceCapability.asStateFlow()

    private val _validationMessage = MutableStateFlow<String?>(null)
    val validationMessage = _validationMessage.asStateFlow()

    private val _validationSuccess = MutableStateFlow<Boolean?>(null)
    val validationSuccess = _validationSuccess.asStateFlow()

    private val _validatingInProgress = MutableStateFlow(false)
    val validatingInProgress = _validatingInProgress.asStateFlow()

    private val _scanResult = MutableStateFlow<Int?>(null)
    val scanResult = _scanResult.asStateFlow()

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError = _downloadError.asStateFlow()

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
            // Sync provider selection to match the execution mode
            val currentSettings = aiSettings.value
            val newProviderId = when (mode) {
                AiExecutionMode.CLOUD -> {
                    // Keep current cloud provider, or default to Claude
                    if (currentSettings.selectedProviderId == AiProviderId.LOCAL.key) {
                        AiProviderId.CLAUDE.key
                    } else {
                        currentSettings.selectedProviderId
                    }
                }
                AiExecutionMode.CUSTOM_LOCAL, AiExecutionMode.SYSTEM_LOCAL -> AiProviderId.LOCAL.key
                AiExecutionMode.AUTO -> currentSettings.selectedProviderId
            }
            if (newProviderId != currentSettings.selectedProviderId) {
                settingsRepo.setAiSettings(currentSettings.copy(selectedProviderId = newProviderId))
            }
            // Reset validation when mode changes
            _validationSuccess.value = null
            _validationMessage.value = null
        }
    }

    fun downloadModel(model: LocalAiModel) {
        _downloadError.value = null
        val url = model.downloadUrl
        if (url.isNullOrBlank()) {
            _downloadError.value = model.modelId
            return
        }
        viewModelScope.launch {
            modelInstaller.downloadModel(model, url)
        }
    }

    fun clearDownloadError() {
        _downloadError.value = null
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
            _scanResult.value = null
            val found = modelInstaller.scanForModels()
            _scanResult.value = found
        }
    }

    fun getCompatibilityReport(model: LocalAiModel): String? {
        val report = compatValidator.validate(model, _deviceCapability.value)
        return if (report.isCompatible) null else report.issues.joinToString("; ")
    }

    fun validateCurrentProvider() {
        viewModelScope.launch {
            _validatingInProgress.value = true
            _validationSuccess.value = null
            _validationMessage.value = null
            try {
                val currentSettings = settingsRepo.getUserSettings().first().aiSettings
                val mode = executionMode.value

                // Mode-aware validation
                when (mode) {
                    AiExecutionMode.CLOUD -> {
                        val result = aiService.validateActiveProvider(currentSettings)
                        _validationSuccess.value = result.valid
                        _validationMessage.value = result.errorMessage
                    }
                    AiExecutionMode.CUSTOM_LOCAL -> {
                        val active = localModelManager.getActiveModel()
                        if (active == null) {
                            _validationSuccess.value = false
                            _validationMessage.value = null // will use R.string.ai_validate_local_no_model
                        } else {
                            val report = compatValidator.validate(active, _deviceCapability.value)
                            _validationSuccess.value = report.isCompatible
                            _validationMessage.value = if (report.isCompatible) null
                                else report.issues.joinToString("; ")
                        }
                    }
                    AiExecutionMode.SYSTEM_LOCAL -> {
                        val cap = _deviceCapability.value
                        val available = cap?.supportsSystemAi == true
                        _validationSuccess.value = available
                        _validationMessage.value = if (!available)
                            "Android AICore is not available on this device" else null
                    }
                    AiExecutionMode.AUTO -> {
                        // Validate all available paths
                        val result = aiService.validateActiveProvider(currentSettings)
                        _validationSuccess.value = result.valid
                        _validationMessage.value = result.errorMessage
                    }
                }
            } catch (e: Exception) {
                _validationSuccess.value = false
                _validationMessage.value = e.message
            } finally {
                _validatingInProgress.value = false
            }
        }
    }
}
