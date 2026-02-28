package com.healthjournal.presentation.screen.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
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
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
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

            when (selectedTab) {
                0 -> RecordsTab(symptoms, vitals, medications, context, viewModel)
                1 -> AiAnalyzeTab(reports, vitals, uiState, context, viewModel)
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
    uiState: AiReportUiState,
    context: android.content.Context,
    viewModel: AiReportViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Generate buttons
        item(key = "ai_buttons") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.generateReport() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.report_7_days))
                }
                OutlinedButton(
                    onClick = { viewModel.analyzePatterns() },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.patterns_30_days))
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

        // Report list
        items(reports, key = { "report_${it.id}" }) { report ->
            ReportCard(report = report, onExportPdf = {
                val memberName = viewModel.getProfileName(report.profileId)
                PdfReportGenerator.generateAndShare(context, report, vitals, memberName)
            })
        }
    }
}

@Composable
private fun ReportCard(report: AiReport, onExportPdf: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val typeLabel = when (report.type) {
        ReportType.SUMMARY -> stringResource(R.string.report_type_summary)
        ReportType.PATTERN_ANALYSIS -> stringResource(R.string.report_type_patterns)
    }
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(typeLabel, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.days_format, report.periodDays), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onExportPdf) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.export_pdf), tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(report.generatedAt.format(formatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text(report.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
