package com.healthjournal.presentation.screen.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.ReportType
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.Medication
import com.healthjournal.presentation.components.VitalsChart
import com.healthjournal.util.PdfReportGenerator
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: AiReportViewModel = viewModel()
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val symptoms by viewModel.symptoms.collectAsStateWithLifecycle()
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val aiEnabled by viewModel.aiEnabled.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val diseases by viewModel.diseases.collectAsStateWithLifecycle()
    val selectedDiseaseId by viewModel.selectedDiseaseId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.reports_tab_records),
        stringResource(R.string.reports_tab_ai)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.reports_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Profile selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeProfileId == 0L,
                    onClick = { viewModel.selectProfile(0L) },
                    label = { Text(stringResource(R.string.rel_self)) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier.size(24.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                )
                familyMembers.forEach { member ->
                    FilterChip(
                        selected = activeProfileId == member.id,
                        onClick = { viewModel.selectProfile(member.id) },
                        label = { Text(member.name) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                    .background(Color(member.avatarColor)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(member.name.take(1).uppercase(), color = Color.White,
                                    style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
            }

            // Disease filter
            if (diseases.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDiseaseId == 0L,
                        onClick = { viewModel.selectDisease(0L) },
                        label = { Text(stringResource(R.string.all_diseases)) }
                    )
                    diseases.forEach { disease ->
                        FilterChip(
                            selected = selectedDiseaseId == disease.id,
                            onClick = { viewModel.selectDisease(disease.id) },
                            label = { Text(disease.name) }
                        )
                    }
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(title, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    )
                }
            }

            // Filter data by selected disease
            val filteredVitals = remember(vitals, selectedDiseaseId) {
                if (selectedDiseaseId == 0L) vitals else vitals.filter { it.diseaseId == selectedDiseaseId }
            }
            val filteredSymptoms = remember(symptoms, selectedDiseaseId) {
                if (selectedDiseaseId == 0L) symptoms else symptoms.filter { it.diseaseId == selectedDiseaseId }
            }
            val filteredMedications = remember(medications, selectedDiseaseId) {
                if (selectedDiseaseId == 0L) medications else medications.filter { it.diseaseId == selectedDiseaseId }
            }

            when (selectedTab) {
                0 -> RecordsTab(filteredSymptoms, filteredVitals, filteredMedications, context, viewModel)
                1 -> AiAnalyzeTab(reports, filteredVitals, filteredSymptoms, uiState, aiEnabled, context, viewModel)
            }
        }
    }
}

@Composable
private fun DateHeader(
    date: LocalDate,
    todayLabel: String,
    yesterdayLabel: String,
    dateFormatter: DateTimeFormatter
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(dateFormatter)
    }
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun RecordsTab(
    symptoms: List<Symptom>,
    vitals: List<VitalSign>,
    medications: List<Medication>,
    context: android.content.Context,
    viewModel: AiReportViewModel
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val memberName = viewModel.getProfileName(viewModel.activeProfileId.collectAsStateWithLifecycle().value)
    val todayLabel = stringResource(R.string.date_today)
    val yesterdayLabel = stringResource(R.string.date_yesterday)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Export PDF button
        if (symptoms.isNotEmpty() || vitals.isNotEmpty()) {
            item(key = "export_records") {
                OutlinedButton(
                    onClick = {
                        PdfReportGenerator.generateRecordsPdfAndShare(
                            context, symptoms, vitals, medications, memberName
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.records_export_pdf))
                }
            }
        }

        // Vitals records
        if (vitals.isNotEmpty()) {
            item(key = "vitals_section") {
                Text(stringResource(R.string.vitals_title),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 8.dp))
            }

            val vitalsByDate = vitals.sortedByDescending { it.recordedAt }
                .groupBy { it.recordedAt.toLocalDate() }

            vitalsByDate.forEach { (date, vitalsForDate) ->
                item(key = "rv_date_$date") {
                    DateHeader(date, todayLabel, yesterdayLabel, dateFormatter)
                }
                items(vitalsForDate, key = { "rv_${it.id}" }) { vital ->
                    val valueStr = if (vital.secondaryValue != null) "${vital.value.toInt()}/${vital.secondaryValue.toInt()}" else vital.value.toString()
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(vital.type.localizedDisplayName(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(vital.recordedAt.format(timeFormatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Text("$valueStr ${vital.type.localizedUnit()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Symptoms records
        if (symptoms.isNotEmpty()) {
            item(key = "symptoms_section") {
                Text(stringResource(R.string.symptoms_title),
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp))
            }

            val symptomsByDate = symptoms.sortedByDescending { it.recordedAt }
                .groupBy { it.recordedAt.toLocalDate() }

            symptomsByDate.forEach { (date, symptomsForDate) ->
                item(key = "rs_date_$date") {
                    DateHeader(date, todayLabel, yesterdayLabel, dateFormatter)
                }
                items(symptomsForDate, key = { "rs_${it.id}" }) { symptom ->
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(symptom.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (symptom.notes.isNotBlank()) {
                                    Text(symptom.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                }
                                Text(symptom.recordedAt.format(timeFormatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Text("${symptom.intensity}/10", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        if (symptoms.isEmpty() && vitals.isEmpty()) {
            item(key = "empty_records") {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_records_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun AiAnalyzeTab(
    reports: List<AiReport>,
    vitals: List<VitalSign>,
    symptoms: List<Symptom>,
    uiState: AiReportUiState,
    aiEnabled: Boolean,
    context: android.content.Context,
    viewModel: AiReportViewModel
) {
    val uniqueDays = remember(vitals, symptoms) {
        (vitals.map { it.recordedAt.toLocalDate() } + symptoms.map { it.recordedAt.toLocalDate() })
            .distinct()
    }
    val hasEnoughData = uniqueDays.size >= 2

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Generate buttons
        item(key = "ai_buttons") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.generateReport() },
                        enabled = !uiState.isLoading && hasEnoughData && aiEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.report_2_days))
                    }
                    OutlinedButton(
                        onClick = { viewModel.analyzePatterns() },
                        enabled = !uiState.isLoading && hasEnoughData && aiEnabled,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.patterns_4_days))
                    }
                }
                if (!aiEnabled) {
                    Text(
                        stringResource(R.string.ai_disabled_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (!hasEnoughData) {
                    Text(
                        stringResource(R.string.ai_need_more_data),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Loading
        if (uiState.isLoading) {
            item(key = "loading") {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.ai_analyzing))
                    }
                }
            }
        }

        // Error
        uiState.error?.let { error ->
            item(key = "error") {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.error_format, error), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = { viewModel.clearError() }) { Text(stringResource(R.string.close)) }
                    }
                }
            }
        }

        // Vitals charts
        val vitalsByType = vitals.groupBy { it.type }
        val chartTypes = vitalsByType.filter { it.value.size >= 2 }
        if (chartTypes.isNotEmpty()) {
            item(key = "charts_header") {
                Text(stringResource(R.string.vitals_charts_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            chartTypes.forEach { (type, typeVitals) ->
                item(key = "chart_${type.name}") {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        VitalsChart(
                            title = type.displayName,
                            vitals = typeVitals.sortedBy { it.recordedAt },
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    }
                }
            }
        }

        // Empty state
        if (reports.isEmpty() && !uiState.isLoading) {
            item(key = "empty_ai") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.no_reports_hint), modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Latest report (shown prominently)
        if (reports.isNotEmpty()) {
            val latestReport = reports.first()
            item(key = "latest_report_${latestReport.id}") {
                ReportCard(
                    report = latestReport,
                    diseaseName = if (latestReport.diseaseId != 0L) viewModel.getDiseaseName(latestReport.diseaseId) else null,
                    onExportPdf = {
                        val memberName = viewModel.getProfileName(latestReport.profileId)
                        PdfReportGenerator.generateAndShare(context, latestReport, vitals, memberName)
                    },
                    onDelete = { viewModel.deleteReport(latestReport.id) }
                )
            }
        }

        // History (collapsible older reports)
        val olderReports = if (reports.size > 1) reports.drop(1) else emptyList()
        if (olderReports.isNotEmpty()) {
            item(key = "history_header") {
                var showHistory by remember { mutableStateOf(false) }
                Column {
                    TextButton(
                        onClick = { showHistory = !showHistory }
                    ) {
                        Text(
                            stringResource(
                                if (showHistory) R.string.ai_hide_history
                                else R.string.ai_show_history,
                                olderReports.size
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    AnimatedVisibility(visible = showHistory) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            olderReports.forEach { report ->
                                ReportCard(
                                    report = report,
                                    diseaseName = if (report.diseaseId != 0L) viewModel.getDiseaseName(report.diseaseId) else null,
                                    onExportPdf = {
                                        val memberName = viewModel.getProfileName(report.profileId)
                                        PdfReportGenerator.generateAndShare(context, report, vitals, memberName)
                                    },
                                    onDelete = { viewModel.deleteReport(report.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: AiReport, diseaseName: String? = null, onExportPdf: () -> Unit, onDelete: (() -> Unit)? = null) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val typeLabel = when (report.type) {
        ReportType.SUMMARY -> stringResource(R.string.report_type_summary)
        ReportType.PATTERN_ANALYSIS -> stringResource(R.string.report_type_patterns)
    }
    var showPrompt by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(typeLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (diseaseName != null) {
                        Text(diseaseName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf), tint = MaterialTheme.colorScheme.primary)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.local_model_delete), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Metadata row: date, period, AI model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(report.generatedAt.format(formatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text("\u2022", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(stringResource(R.string.days_format, report.periodDays), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }

            // AI provider badge
            if (report.providerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    tonalElevation = 0.dp
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

            // Report content
            Text(report.content, style = MaterialTheme.typography.bodyMedium)

            // Expandable prompt section
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
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 0.dp
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
