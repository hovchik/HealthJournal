package com.healthjournal.presentation.screen.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Person
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
import com.healthjournal.domain.model.VitalType
import com.healthjournal.presentation.components.VitalsChart
import com.healthjournal.util.PdfReportGenerator
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiReportScreen(
    viewModel: AiReportViewModel = viewModel()
) {
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.ai_reports_title),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile selector
            item {
                Text(
                    stringResource(R.string.report_select_profile),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Self chip
                    FilterChip(
                        selected = activeProfileId == 0L,
                        onClick = { viewModel.selectProfile(0L) },
                        label = { Text(stringResource(R.string.rel_self)) },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
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
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(member.avatarColor)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        member.name.take(1).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Generate buttons
            item {
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
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
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

            // Vitals charts
            val vitalsByType = vitals.groupBy { it.type }
            val chartTypes = vitalsByType.filter { it.value.size >= 2 }
            if (chartTypes.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.vitals_charts_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                chartTypes.forEach { (type, typeVitals) ->
                    item(key = "chart_${type.name}") {
                        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                            VitalsChart(
                                title = type.displayName,
                                vitals = typeVitals.sortedBy { it.recordedAt },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }

            // Empty state
            if (reports.isEmpty() && !uiState.isLoading) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.no_reports_hint),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Report list
            items(reports, key = { "report_${it.id}" }) { report ->
                ReportCard(
                    report = report,
                    onExportPdf = {
                        val memberName = viewModel.getProfileName(report.profileId)
                        PdfReportGenerator.generateAndShare(context, report, vitals, memberName)
                    }
                )
            }
        }
    }
}

@Composable
private fun ReportCard(
    report: AiReport,
    onExportPdf: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val typeLabel = when (report.type) {
        ReportType.SUMMARY -> stringResource(R.string.report_type_summary)
        ReportType.PATTERN_ANALYSIS -> stringResource(R.string.report_type_patterns)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(typeLabel, style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(R.string.days_format, report.periodDays),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onExportPdf) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = stringResource(R.string.export_pdf),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                report.generatedAt.format(formatter),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(report.content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
