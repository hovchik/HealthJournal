package com.healthjournal.presentation.screen.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.presentation.theme.LocalHealthExtended
import java.time.LocalDate
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    isTopLevel: Boolean = false,
    onAddSymptom: () -> Unit = {},
    onAddVital: () -> Unit = {},
    onAddMedication: () -> Unit = {},
    onAiChat: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

    val activeProfileName = if (activeProfileId == 0L) {
        stringResource(R.string.rel_self)
    } else {
        familyMembers.find { it.id == activeProfileId }?.name
            ?: stringResource(R.string.rel_self)
    }

    var showQuickAdd by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current

    val pulseSeries = remember(state.symptomsByDay) {
        val today = LocalDate.now()
        (13 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            (state.symptomsByDay[day] ?: 0).toFloat()
        }
    }

    val isEmpty = state.symptoms.isEmpty() && state.vitals.isEmpty()

    Scaffold(
        containerColor = cs.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = ext.signal,
                contentColor = ext.onSignal,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.quick_add_title))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item(key = "hero") {
                DashboardHero(
                    profileName = activeProfileName,
                    isTopLevel = isTopLevel,
                    onBack = onBack,
                    pulse = { mod -> AmbientPulse(series = pulseSeries, modifier = mod) }
                )
            }

            item(key = "period") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cs.background)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    PeriodSegmentedControl(
                        options = listOf(3, 7, 14, 30, 90),
                        selected = state.selectedPeriodDays,
                        onSelect = viewModel::setPeriod
                    )
                }
            }

            if (isEmpty) {
                item(key = "empty") {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        DashboardEmptyState()
                    }
                }
            } else {
                item(key = "bento") {
                    DashboardBento(
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onAddSymptom = onAddSymptom,
            onAddVital = onAddVital,
            onAddMedication = onAddMedication
        )
    }
}

// ── Bento layout ──────────────────────────────────────────────────────────────
// Two-column stat row, then full-bleed pulse, trends, and signals. Sections
// are introduced with quiet labels rather than card headers — the page reads
// like a chart, not a list.
@Composable
private fun DashboardBento(
    state: DashboardUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel(text = stringResource(R.string.dashboard_section_glance))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatTile(
                label = stringResource(R.string.symptoms_title),
                value = "${state.symptoms.size}",
                icon = Icons.Default.Sick,
                accent = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = stringResource(R.string.nav_vitals),
                value = "${state.vitals.size}",
                icon = Icons.Default.MonitorHeart,
                accent = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        if (state.symptomsByDay.isNotEmpty()) {
            SymptomPulseTile(
                symptomsByDay = state.symptomsByDay,
                days = state.selectedPeriodDays,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val trendTypes = state.vitalsByType.filterValues { it.size >= 2 }
        if (trendTypes.isNotEmpty()) {
            SectionLabel(text = stringResource(R.string.dashboard_section_trends))
            for ((type, vitals) in trendTypes) {
                VitalTrendTile(
                    type = type,
                    vitals = vitals,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.correlations.isNotEmpty()) {
            SectionLabel(text = stringResource(R.string.dashboard_section_signals))
            for (c in state.correlations) {
                CorrelationStripCard(item = c, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// ── Ambient pulse sparkline ───────────────────────────────────────────────────
// Low-contrast 14-day mini chart drawn behind the hero text. Animates its
// stroke in via a horizontal clip so the line "writes itself" on entry.
@Composable
private fun AmbientPulse(series: List<Float>, modifier: Modifier = Modifier) {
    val ext = LocalHealthExtended.current

    val progress = remember(series) { Animatable(0f) }
    LaunchedEffect(series) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 800, easing = EaseOutCubic))
    }

    Canvas(modifier = modifier.alpha(0.45f)) {
        if (series.isEmpty()) return@Canvas
        val maxV = max(series.max(), 1f)
        val n = series.size

        val fillPath = Path()
        val strokePath = Path()
        series.forEachIndexed { i, v ->
            val x = size.width * i / (n - 1).coerceAtLeast(1)
            val y = size.height * (1f - 0.65f * (v / maxV)) - 8f
            if (i == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        val clipWidth = size.width * progress.value
        clipRect(left = 0f, top = 0f, right = clipWidth, bottom = size.height) {
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    0f to ext.signal.copy(alpha = 0.25f),
                    1f to Color.Transparent
                )
            )
            drawPath(
                strokePath,
                color = ext.signal,
                style = Stroke(width = 2.5f, cap = StrokeCap.Round)
            )
        }
    }
}
