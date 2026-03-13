package com.healthjournal.presentation.screen.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.style.TextOverflow
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
    var selectedPeriod by remember { mutableIntStateOf(2) }
    val periodOptions = listOf(2, 4, 7, 14)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Generate controls
        item(key = "ai_controls") {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Period selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.report_period_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        periodOptions.forEach { days ->
                            FilterChip(
                                selected = selectedPeriod == days,
                                onClick = { selectedPeriod = days },
                                label = {
                                    Text(
                                        stringResource(R.string.report_period_days, days),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }

                    // Generate buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.generateReport(selectedPeriod) },
                            enabled = !uiState.isLoading && hasEnoughData && aiEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.report_generate_summary))
                        }
                        OutlinedButton(
                            onClick = { viewModel.analyzePatterns(selectedPeriod) },
                            enabled = !uiState.isLoading && hasEnoughData && aiEnabled,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.report_generate_patterns))
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

/**
 * Parses structured report content into sections.
 * Recognizes "=== Section Title ===" headers and splits content accordingly.
 */
private data class ReportSection(val title: String, val body: String)

private fun parseReportContent(content: String): List<ReportSection> {
    if (content.isBlank()) return emptyList()

    val sectionPattern = Regex("""^===\s*(.+?)\s*===$""", RegexOption.MULTILINE)
    val matches = sectionPattern.findAll(content).toList()

    if (matches.isEmpty()) {
        // No section headers found - treat as single block
        return listOf(ReportSection("", content.trim()))
    }

    val sections = mutableListOf<ReportSection>()

    // Content before first section header (title/note)
    val preamble = content.substring(0, matches.first().range.first).trim()
    if (preamble.isNotBlank()) {
        sections.add(ReportSection("", preamble))
    }

    // Parse each section
    for (i in matches.indices) {
        val title = matches[i].groupValues[1]
        val start = matches[i].range.last + 1
        val end = if (i + 1 < matches.size) matches[i + 1].range.first else content.length
        val body = content.substring(start, end).trim()
        if (body.isNotBlank()) {
            sections.add(ReportSection(title, body))
        }
    }

    // Disclaimer at the end (text after last section, if not captured)
    return sections
}

@Composable
private fun ReportCard(report: AiReport, diseaseName: String? = null, onExportPdf: () -> Unit, onDelete: (() -> Unit)? = null) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val typeLabel = when (report.type) {
        ReportType.SUMMARY -> stringResource(R.string.report_type_summary)
        ReportType.PATTERN_ANALYSIS -> stringResource(R.string.report_type_patterns)
    }
    val context = LocalContext.current
    val copiedMsg = stringResource(R.string.report_copied)
    var expanded by remember { mutableStateOf(true) }
    val sections = remember(report.content) { parseReportContent(report.content) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(typeLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (diseaseName != null) {
                        Text(
                            diseaseName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                // Expand/collapse
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Metadata row
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
                Text("\u2022", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(
                    stringResource(R.string.days_format, report.periodDays),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
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

            // Action buttons row
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
                IconButton(onClick = onExportPdf, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.local_model_delete), modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Report content - parsed into sections
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
                                // Preamble (title/note without section header)
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
