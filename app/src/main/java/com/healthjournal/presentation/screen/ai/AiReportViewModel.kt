package com.healthjournal.presentation.screen.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.ai.AiSettings
import com.healthjournal.util.LocaleManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AiReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AiReportViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val diseases = combine(container.getAllDiseases(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedDiseaseId = MutableStateFlow(0L)
    val selectedDiseaseId = _selectedDiseaseId.asStateFlow()

    val reports = combine(container.getAllReports(), activeProfileFlow, _selectedDiseaseId) { all, profileId, diseaseId ->
        all.filter { it.profileId == profileId && it.diseaseId == diseaseId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val vitals = combine(container.getAllVitalSigns(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val symptoms = combine(container.getAllSymptoms(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val medications = combine(container.getAllMedications(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState = _uiState.asStateFlow()

    private suspend fun getAiSettings(): AiSettings =
        container.userSettingsRepository.getUserSettings().first().aiSettings

    private fun getOutputLanguage(): String =
        LocaleManager.getCurrentLanguageTag(getApplication())

    fun selectProfile(profileId: Long) {
        viewModelScope.launch {
            container.userSettingsRepository.setActiveProfileId(profileId)
        }
    }

    fun selectDisease(diseaseId: Long) {
        _selectedDiseaseId.value = diseaseId
    }

    val aiEnabled = container.userSettingsRepository.getUserSettings()
        .map { it.aiSettings.enabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun generateReport(periodDays: Int = 2) {
        viewModelScope.launch {
            val settings = getAiSettings()
            if (!settings.enabled) {
                _uiState.value = AiReportUiState(error = "AI is disabled. Enable it in Settings > AI Settings.")
                return@launch
            }
            _uiState.value = AiReportUiState(isLoading = true)
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            val diseaseId = _selectedDiseaseId.value
            container.generateAiSummary(periodDays, getOutputLanguage(), settings, profileId, diseaseId)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun analyzePatterns(periodDays: Int = 4) {
        viewModelScope.launch {
            val settings = getAiSettings()
            if (!settings.enabled) {
                _uiState.value = AiReportUiState(error = "AI is disabled. Enable it in Settings > AI Settings.")
                return@launch
            }
            _uiState.value = AiReportUiState(isLoading = true)
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            val diseaseId = _selectedDiseaseId.value
            container.generatePatternAnalysis(periodDays, getOutputLanguage(), settings, profileId, diseaseId)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getProfileName(profileId: Long): String {
        if (profileId == 0L) return "Self"
        return familyMembers.value.firstOrNull { it.id == profileId }?.name ?: "Self"
    }

    fun getDiseaseName(diseaseId: Long): String? =
        diseases.value.firstOrNull { it.id == diseaseId }?.name
}
