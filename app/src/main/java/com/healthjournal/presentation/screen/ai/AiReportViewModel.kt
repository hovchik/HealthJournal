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

    val reports = combine(container.getAllReports(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
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

    fun generateReport(periodDays: Int = 2) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            val settings = getAiSettings()
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.generateAiSummary(periodDays, getOutputLanguage(), settings, profileId)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun analyzePatterns(periodDays: Int = 4) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            val settings = getAiSettings()
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.generatePatternAnalysis(periodDays, getOutputLanguage(), settings, profileId)
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
}
