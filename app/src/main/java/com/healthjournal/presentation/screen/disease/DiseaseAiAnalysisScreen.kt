package com.healthjournal.presentation.screen.disease

import android.app.Application
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.HealthJournalApp
import com.healthjournal.R
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.ai.AiSettings
import com.healthjournal.util.LocaleManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

data class DiseaseAiUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class DiseaseAiAnalysisViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val _uiState = MutableStateFlow(DiseaseAiUiState())
    val uiState = _uiState.asStateFlow()

    val aiEnabled = container.userSettingsRepository.getUserSettings()
        .map { it.aiSettings.enabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    private val _diseaseReports = MutableStateFlow<List<AiReport>>(emptyList())
    val diseaseReports = _diseaseReports.asStateFlow()

    private var _diseaseName = MutableStateFlow("")
    val diseaseName = _diseaseName.asStateFlow()

    fun loadDisease(diseaseId: Long) {
        viewModelScope.launch {
            val disease = container.getDiseaseById(diseaseId)
            _diseaseName.value = disease?.name ?: ""

            container.getAllReports().collect { allReports ->
                _diseaseReports.value = allReports.filter { it.diseaseId == diseaseId }
                    .sortedByDescending { it.generatedAt }
            }
        }
    }

    private suspend fun getAiSettings(): AiSettings =
        container.userSettingsRepository.getUserSettings().first().aiSettings

    private fun getOutputLanguage(): String =
        LocaleManager.getCurrentLanguageTag(getApplication())

    fun generateDiseaseAnalysis(diseaseId: Long) {
        viewModelScope.launch {
            val settings = getAiSettings()
            if (!settings.enabled) {
                _uiState.value = DiseaseAiUiState(error = "AI is disabled. Enable it in Settings > AI Settings.")
                return@launch
            }
            _uiState.value = DiseaseAiUiState(isLoading = true)
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.generateDiseaseAnalysis(diseaseId, getOutputLanguage(), settings, profileId)
                .onSuccess {
                    _uiState.value = DiseaseAiUiState()
                    // Refresh reports
                    val allReports = container.getAllReports().first()
                    _diseaseReports.value = allReports.filter { r -> r.diseaseId == diseaseId }
                        .sortedByDescending { r -> r.generatedAt }
                }
                .onFailure { _uiState.value = DiseaseAiUiState(error = it.message) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun deleteReport(reportId: Long, diseaseId: Long) {
        viewModelScope.launch {
            val report = _diseaseReports.value.firstOrNull { it.id == reportId } ?: return@launch
            container.aiReportRepository.deleteReport(report)
            val allReports = container.getAllReports().first()
            _diseaseReports.value = allReports.filter { r -> r.diseaseId == diseaseId }
                .sortedByDescending { r -> r.generatedAt }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseAiAnalysisScreen(
    diseaseId: Long,
    onBack: () -> Unit,
    viewModel: DiseaseAiAnalysisViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val reports by viewModel.diseaseReports.collectAsStateWithLifecycle()
    val diseaseName by viewModel.diseaseName.collectAsStateWithLifecycle()

    LaunchedEffect(diseaseId) {
        viewModel.loadDisease(diseaseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.disease_ai_analysis_title),
                            fontWeight = FontWeight.Bold
                        )
                        if (diseaseName.isNotBlank()) {
                            Text(
                                diseaseName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Generate button
            item(key = "generate_btn") {
                Button(
                    onClick = { viewModel.generateDiseaseAnalysis(diseaseId) },
                    enabled = !uiState.isLoading && aiEnabled,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.disease_ai_generate))
                }
                if (!aiEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.ai_disabled_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Loading
            if (uiState.isLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.disease_ai_analyzing))
                        }
                    }
                }
            }

            // Error
            uiState.error?.let { error ->
                item(key = "error") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.error_format, error),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = { viewModel.clearError() }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                }
            }

            // Reports list
            if (reports.isEmpty() && !uiState.isLoading) {
                item(key = "empty") {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.disease_ai_no_reports),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(reports, key = { "report_${it.id}" }) { report ->
                DiseaseAnalysisReportCard(
                    report = report,
                    onDelete = { viewModel.deleteReport(report.id, diseaseId) }
                )
            }
        }
    }
}

@Composable
private fun DiseaseAnalysisReportCard(
    report: AiReport,
    onDelete: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    var showPrompt by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.disease_ai_report_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Date and provider
            Text(
                report.generatedAt.format(formatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            if (report.providerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.SmartToy,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            stringResource(R.string.report_ai_model, report.providerName),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Content
            Text(report.content, style = MaterialTheme.typography.bodyMedium)

            // Expandable prompt
            if (report.prompt.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showPrompt = !showPrompt },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Text(
                        stringResource(if (showPrompt) R.string.report_hide_prompt else R.string.report_show_prompt),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                AnimatedVisibility(visible = showPrompt) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            report.prompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
