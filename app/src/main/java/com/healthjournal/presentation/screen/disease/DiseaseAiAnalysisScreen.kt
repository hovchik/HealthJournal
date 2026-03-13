package com.healthjournal.presentation.screen.disease

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private data class DiseaseReportSection(val title: String, val body: String)

private fun parseDiseaseReportContent(content: String): List<DiseaseReportSection> {
    if (content.isBlank()) return emptyList()
    val sectionPattern = Regex("""^===\s*(.+?)\s*===$""", RegexOption.MULTILINE)
    val matches = sectionPattern.findAll(content).toList()
    if (matches.isEmpty()) return listOf(DiseaseReportSection("", content.trim()))

    val sections = mutableListOf<DiseaseReportSection>()
    val preamble = content.substring(0, matches.first().range.first).trim()
    if (preamble.isNotBlank()) sections.add(DiseaseReportSection("", preamble))

    for (i in matches.indices) {
        val title = matches[i].groupValues[1]
        val start = matches[i].range.last + 1
        val end = if (i + 1 < matches.size) matches[i + 1].range.first else content.length
        val body = content.substring(start, end).trim()
        if (body.isNotBlank()) sections.add(DiseaseReportSection(title, body))
    }
    return sections
}

@Composable
private fun DiseaseAnalysisReportCard(
    report: AiReport,
    onDelete: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.report_copied)
    var expanded by remember { mutableStateOf(true) }
    val sections = remember(report.content) { parseDiseaseReportContent(report.content) }

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
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    report.generatedAt.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (report.providerName.isNotBlank()) {
                    Text("\u2022", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(
                        report.providerName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("report", report.content))
                        Toast.makeText(context, copiedMsg, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.report_copy), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
            }

            // Content - parsed into sections
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (report.content.isBlank()) {
                        Text(
                            stringResource(R.string.report_no_content),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (sections.isEmpty()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(report.content, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        sections.forEach { section ->
                            if (section.title.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                                    tonalElevation = 0.dp
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            section.title,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            section.body,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    section.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
