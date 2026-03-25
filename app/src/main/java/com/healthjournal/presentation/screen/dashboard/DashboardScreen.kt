package com.healthjournal.presentation.screen.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title), fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                PeriodSelector(
                    selectedDays = state.selectedPeriodDays,
                    onSelect = viewModel::setPeriod
                )
            }

            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.symptoms_title),
                        value = "${state.symptoms.size}",
                        icon = Icons.Default.Sick,
                        color = MaterialTheme.colorScheme.error
                    )
                    SummaryCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(R.string.nav_vitals),
                        value = "${state.vitals.size}",
                        icon = Icons.Default.MonitorHeart,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Symptom heatmap
            if (state.symptomsByDay.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.symptom_heatmap_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    SymptomHeatmap(
                        symptomsByDay = state.symptomsByDay,
                        days = state.selectedPeriodDays
                    )
                }
            }

            // Vital trend charts
            for ((type, vitals) in state.vitalsByType) {
                if (vitals.size >= 2) {
                    item(key = "chart_${type.name}") {
                        VitalTrendCard(type = type, vitals = vitals)
                    }
                }
            }

            // Correlations
            if (state.correlations.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.correlations_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(state.correlations) { correlation ->
                    CorrelationCard(correlation)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PeriodSelector(selectedDays: Int, onSelect: (Int) -> Unit) {
    val periods = listOf(3, 7, 14, 30, 90)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(periods) { days ->
            FilterChip(
                selected = selectedDays == days,
                onClick = { onSelect(days) },
                label = { Text(stringResource(R.string.days_format, days)) }
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    ElevatedCard(modifier = modifier, shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SymptomHeatmap(symptomsByDay: Map<LocalDate, Int>, days: Int) {
    val today = LocalDate.now()
    val maxCount = symptomsByDay.values.maxOrNull() ?: 1
    val primaryColor = MaterialTheme.colorScheme.error

    ElevatedCard(shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
            Canvas(
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                val cellSize = size.width / days.coerceAtLeast(1)
                val cellHeight = size.height

                for (i in 0 until days) {
                    val date = today.minusDays((days - 1 - i).toLong())
                    val count = symptomsByDay[date] ?: 0
                    val alpha = if (maxCount > 0) (count.toFloat() / maxCount).coerceIn(0.05f, 1f) else 0.05f

                    drawRect(
                        color = primaryColor.copy(alpha = alpha),
                        topLeft = Offset(i * cellSize + 1, 0f),
                        size = Size(cellSize - 2, cellHeight)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val formatter = DateTimeFormatter.ofPattern("dd.MM")
                Text(
                    today.minusDays((days - 1).toLong()).format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    today.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun VitalTrendCard(type: VitalType, vitals: List<VitalSign>) {
    val sorted = vitals.sortedBy { it.recordedAt }
    val lineColor = MaterialTheme.colorScheme.primary
    val secondaryLineColor = MaterialTheme.colorScheme.tertiary

    ElevatedCard(shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(type.localizedDisplayName(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                if (sorted.size < 2) return@Canvas
                val values = sorted.map { it.value.toFloat() }
                val secondaryValues = sorted.mapNotNull { it.secondaryValue?.toFloat() }
                val allVals = values + secondaryValues
                val minV = allVals.min()
                val maxV = allVals.max()
                val range = (maxV - minV).coerceAtLeast(1f)

                val path = Path()
                values.forEachIndexed { i, v ->
                    val x = size.width * i / (values.size - 1).coerceAtLeast(1)
                    val y = size.height * (1f - (v - minV) / range)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                values.forEachIndexed { i, v ->
                    val x = size.width * i / (values.size - 1).coerceAtLeast(1)
                    val y = size.height * (1f - (v - minV) / range)
                    drawCircle(lineColor, radius = 4f, center = Offset(x, y))
                }

                if (secondaryValues.size == values.size) {
                    val secPath = Path()
                    secondaryValues.forEachIndexed { i, v ->
                        val x = size.width * i / (secondaryValues.size - 1).coerceAtLeast(1)
                        val y = size.height * (1f - (v - minV) / range)
                        if (i == 0) secPath.moveTo(x, y) else secPath.lineTo(x, y)
                    }
                    drawPath(secPath, secondaryLineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            val lastVital = sorted.last()
            val valueStr = if (lastVital.secondaryValue != null) {
                "${lastVital.value.toInt()}/${lastVital.secondaryValue.toInt()}"
            } else "%.1f".format(lastVital.value)
            Text(
                stringResource(R.string.latest_value, valueStr, type.localizedUnit()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CorrelationCard(item: CorrelationItem) {
    ElevatedCard(shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when {
                    item.strength > 0.6f -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                    item.strength > 0.3f -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                }
            ) {
                Text(
                    "${"%.0f".format(item.strength * 100)}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        item.strength > 0.6f -> MaterialTheme.colorScheme.error
                        item.strength > 0.3f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
    }
}
