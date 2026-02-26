package com.healthjournal.presentation.screen.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AiReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AiReportViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val reports = container.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState = _uiState.asStateFlow()

    fun generateReport(periodDays: Int = 7) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            container.generateAiSummary(periodDays)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun analyzePatterns(periodDays: Int = 30) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            container.generatePatternAnalysis(periodDays)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
