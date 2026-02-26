package com.healthjournal.presentation.screen.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.usecase.GenerateAiSummaryUseCase
import com.healthjournal.domain.usecase.GeneratePatternAnalysisUseCase
import com.healthjournal.domain.usecase.GetAllReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiReportUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AiReportViewModel @Inject constructor(
    getAllReports: GetAllReportsUseCase,
    private val generateSummary: GenerateAiSummaryUseCase,
    private val generatePatternAnalysis: GeneratePatternAnalysisUseCase
) : ViewModel() {

    val reports = getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(AiReportUiState())
    val uiState = _uiState.asStateFlow()

    fun generateReport(periodDays: Int = 7) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            generateSummary(periodDays)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun analyzePatterns(periodDays: Int = 30) {
        viewModelScope.launch {
            _uiState.value = AiReportUiState(isLoading = true)
            generatePatternAnalysis(periodDays)
                .onSuccess { _uiState.value = AiReportUiState() }
                .onFailure { _uiState.value = AiReportUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
