package com.hovchik.healthjournal.presentation.screen.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.presentation.theme.LocalHealthExtended
import java.time.LocalDate
import kotlin.math.max

/*
 * Editorial dashboard. Single flat paper surface. Sections delineated by
 * ruled uppercase headers and negative space. The two allowed non-zero radii
 * — the period pill and the FAB — are the only shapes in the room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onBack: () -> Unit,
    isTopLevel: Boolean = false,
    onAddSymptom: () -> Unit = {},
    onAddVital: () -> Unit = {},
    onAddMedication: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onAiChat: () -> Unit = {},
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
    val ext = LocalHealthExtended.current

    val pulseSeries = remember(state.symptomsByDay) {
        val today = LocalDate.now()
        (13 downTo 0).map { offset ->
            val day = today.minusDays(offset.toLong())
            (state.symptomsByDay[day] ?: 0).toFloat()
        }
    }

    val isEmpty = state.symptoms.isEmpty() && state.vitals.isEmpty()
    val trendTypes = state.vitalsByType.filterValues { it.size >= 2 }

    Scaffold(
        containerColor = ext.paper,
        floatingActionButton = {
            // The one FAB, pill-shaped, carrying signal colour. Opening the
            // quick-add sheet is the single "commit" action on this screen.
            FloatingActionButton(
                onClick = { showQuickAdd = true },
                containerColor = ext.signal,
                contentColor = ext.onSignal,
                shape = CircleShape
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
                EditorialHero(
                    profileName = activeProfileName,
                    isTopLevel = isTopLevel,
                    onBack = onBack,
                    pulse = { mod -> AmbientPulse(series = pulseSeries, modifier = mod) }
                )
                Spacer(Modifier.height(16.dp))
                PeriodPill(
                    options = listOf(3, 7, 14, 30, 90),
                    selected = state.selectedPeriodDays,
                    onSelect = viewModel::setPeriod
                )
                Spacer(Modifier.height(12.dp))
            }

            if (isEmpty) {
                item(key = "empty") { EditorialEmpty() }
            } else {
                item(key = "glance-header") {
                    SectionHeader(text = stringResource(R.string.dashboard_section_glance))
                }
                item(key = "glance") {
                    GlanceRow(
                        symptomsCount = state.symptoms.size,
                        vitalsCount = state.vitals.size
                    )
                }

                if (state.symptomsByDay.isNotEmpty()) {
                    item(key = "pulse-header") {
                        SectionHeader(text = stringResource(R.string.dashboard_pulse))
                    }
                    item(key = "pulse") {
                        PulseStrip(
                            symptomsByDay = state.symptomsByDay,
                            days = state.selectedPeriodDays
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                if (trendTypes.isNotEmpty()) {
                    item(key = "trends-header") {
                        SectionHeader(text = stringResource(R.string.dashboard_section_trends))
                    }
                    for ((type, vitals) in trendTypes) {
                        item(key = "trend-${type.name}") {
                            VitalBlock(type = type, vitals = vitals)
                        }
                    }
                }

                if (state.correlations.isNotEmpty()) {
                    item(key = "signals-header") {
                        SectionHeader(text = stringResource(R.string.dashboard_section_signals))
                    }
                    state.correlations.forEachIndexed { index, c ->
                        item(key = "corr-$index") {
                            CorrelationRow(item = c)
                            if (index < state.correlations.lastIndex) HairlineRule()
                        }
                    }
                }

                item(key = "tail") {
                    Spacer(Modifier.height(24.dp))
                    HairlineRule()
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

// ── Ambient pulse ────────────────────────────────────────────────────────────
// Lives inside the hero. 14-day mini sparkline drawn in signal color with a
// horizontal clip that advances 0→1 on composition, so the line "writes
// itself in" alongside the page entering.
@Composable
private fun AmbientPulse(series: List<Float>, modifier: Modifier = Modifier) {
    val ext = LocalHealthExtended.current

    val progress = remember(series) { Animatable(0f) }
    LaunchedEffect(series) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 900, easing = EaseOutCubic))
    }

    Canvas(modifier = modifier) {
        if (series.isEmpty()) return@Canvas
        val maxV = max(series.max(), 1f)
        val lastIndex = (series.size - 1).coerceAtLeast(1)
        val baseline = size.height - 1f

        val strokePath = Path()
        series.forEachIndexed { i, v ->
            val x = size.width * i / lastIndex
            val y = size.height * (1f - 0.85f * (v / maxV)) - 2f
            if (i == 0) strokePath.moveTo(x, y) else strokePath.lineTo(x, y)
        }

        val clipWidth = size.width * progress.value
        clipRect(left = 0f, top = 0f, right = clipWidth, bottom = size.height) {
            // Stroke alone — no gradient fill. Reads as a journalistic chart
            // rule rather than decorative ambient glow.
            drawPath(
                strokePath,
                color = ext.signal,
                style = Stroke(width = 1.5f, cap = StrokeCap.Round)
            )
        }

        // Baseline hairline spanning the full pulse width — anchors the stroke
        drawLine(
            color = ext.hairline,
            start = androidx.compose.ui.geometry.Offset(0f, baseline),
            end = androidx.compose.ui.geometry.Offset(size.width, baseline),
            strokeWidth = 0.5f
        )
    }
}
