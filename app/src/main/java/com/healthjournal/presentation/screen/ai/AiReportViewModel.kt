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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val userSettings = container.userSettingsRepository.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeProfileId = userSettings
        .map { it?.activeProfileId ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val reports = combine(container.getAllReports(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vitals = combine(container.getAllVitalSigns(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptoms = combine(container.getAllSymptoms(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medications = combine(container.getAllMedications(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun generateReport(periodDays: Int = 7) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            val settings = getAiSettings()
            val profileId = activeProfileId.value
            container.generateAiSummary(periodDays, getOutputLanguage(), settings, profileId)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun analyzePatterns(periodDays: Int = 30) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            val settings = getAiSettings()
            val profileId = activeProfileId.value
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
