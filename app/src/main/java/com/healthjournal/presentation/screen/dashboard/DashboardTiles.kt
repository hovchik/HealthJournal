package com.healthjournal.presentation.screen.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthjournal.R
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.presentation.theme.LocalHealthExtended
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max

private val isoFormatter = DateTimeFormatter.ofPattern("dd.MM")

// ── Pulse heatmap tile ────────────────────────────────────────────────────────
// Replaces the single-row alpha-rect heatmap with a dot grid. Each dot's
// saturation maps to symptom count. Tap a column → spring callout shows the
// date and entry count without leaving the tile.
@Composable
fun SymptomPulseTile(
    symptomsByDay: Map<LocalDate, Int>,
    days: Int,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current
    val today = remember(days) { LocalDate.now() }
    val maxCount = remember(symptomsByDay) { symptomsByDay.values.maxOrNull() ?: 0 }

    var selectedIndex by remember(days) { mutableStateOf<Int?>(null) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = cs.surface,
        border = BorderStroke(1.dp, ext.gridInk),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.symptom_heatmap_title),
                style = MaterialTheme.typography.titleSmall,
                color = cs.onSurface
            )
            Spacer(Modifier.height(12.dp))

            val accent = ext.signal
            val baseDot = cs.surfaceContainerHigh

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(days) {
                            detectTapGestures { offset ->
                                val cellW = size.width / days.coerceAtLeast(1)
                                val idx = (offset.x / cellW).toInt().coerceIn(0, days - 1)
                                selectedIndex = if (selectedIndex == idx) null else idx
                            }
                        }
                ) {
                    val cellW = size.width / days.coerceAtLeast(1)
                    val rows = 3
                    val rowSpacing = size.height / (rows + 1)
                    val dotRadius = (cellW.coerceAtMost(rowSpacing) * 0.30f).coerceAtLeast(2f)

                    for (i in 0 until days) {
                        val date = today.minusDays((days - 1 - i).toLong())
                        val count = symptomsByDay[date] ?: 0
                        val intensity = if (maxCount > 0) {
                            (count.toFloat() / maxCount).coerceIn(0f, 1f)
                        } else 0f

                        // Three stacked dots per column form the "pulse" texture.
                        for (r in 0 until rows) {
                            val centerX = i * cellW + cellW / 2f
                            val centerY = rowSpacing * (r + 1)
                            val rowAlpha = when (r) {
                                1 -> intensity                          // strongest mid-row
                                else -> (intensity * 0.55f).coerceIn(0f, 1f)
                            }
                            val color = if (rowAlpha < 0.05f) baseDot
                            else accent.copy(alpha = (0.25f + rowAlpha * 0.75f).coerceIn(0f, 1f))

                            drawCircle(
                                color = color,
                                radius = dotRadius,
                                center = Offset(centerX, centerY)
                            )
                        }

                        if (selectedIndex == i) {
                            // Highlight ring around the selected column
                            drawRect(
                                color = accent.copy(alpha = 0.10f),
                                topLeft = Offset(i * cellW + 2f, 0f),
                                size = Size(cellW - 4f, size.height)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedVisibility(
                visible = selectedIndex != null,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                    expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = fadeOut(tween(120)) + shrinkVertically(tween(160))
            ) {
                val idx = selectedIndex ?: 0
                val date = today.minusDays((days - 1 - idx).toLong())
                val count = symptomsByDay[date] ?: 0
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cs.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = date.format(isoFormatter),
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onSurface
                        )
                        Text(
                            text = stringResource(R.string.dashboard_entries_for, count),
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = ext.numerals),
                            color = if (count > 0) ext.signal else cs.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = today.minusDays((days - 1).toLong()).format(isoFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.outline
                )
                Text(
                    text = today.format(isoFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.outline
                )
            }
        }
    }
}

// ── Vital trend tile ──────────────────────────────────────────────────────────
// Big monospace headline + draw-on-load sparkline. The path is drawn over
// ~600ms via PathMeasure.getSegment, then dots fade in staggered.
@Composable
fun VitalTrendTile(
    type: VitalType,
    vitals: List<VitalSign>,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current
    val sorted = remember(vitals) { vitals.sortedBy { it.recordedAt } }
    val unit = type.localizedUnit()

    val latest = sorted.last()
    val displayValue = if (latest.secondaryValue != null) {
        "${latest.value.toInt()}/${latest.secondaryValue.toInt()}"
    } else "%.1f".format(latest.value)

    val drawProgress = remember(vitals) { Animatable(0f) }
    LaunchedEffect(vitals) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 620, easing = EaseOutCubic)
        )
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = cs.surface,
        border = BorderStroke(1.dp, ext.gridInk),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = type.localizedDisplayName().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = ext.numerals),
                    color = cs.onSurface
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            val lineColor = cs.primary
            val secondaryLineColor = ext.data
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                if (sorted.size < 2) return@Canvas
                val values = sorted.map { it.value.toFloat() }
                val secondary = sorted.mapNotNull { it.secondaryValue?.toFloat() }
                val all = values + secondary
                val minV = all.min()
                val maxV = all.max()
                val range = max(maxV - minV, 1f)

                fun pathFor(series: List<Float>): Path {
                    val p = Path()
                    series.forEachIndexed { i, v ->
                        val x = size.width * i / (series.size - 1).coerceAtLeast(1)
                        val y = size.height * (1f - (v - minV) / range)
                        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
                    }
                    return p
                }

                fun drawAnimated(series: List<Float>, color: Color) {
                    val full = pathFor(series)
                    val measure = PathMeasure().apply { setPath(full, false) }
                    val visible = Path()
                    measure.getSegment(0f, measure.length * drawProgress.value, visible, true)
                    drawPath(visible, color, style = Stroke(width = 3f, cap = StrokeCap.Round))

                    val pointFraction = drawProgress.value
                    series.forEachIndexed { i, v ->
                        val pointTime = i.toFloat() / (series.size - 1).coerceAtLeast(1)
                        if (pointTime <= pointFraction) {
                            val x = size.width * i / (series.size - 1).coerceAtLeast(1)
                            val y = size.height * (1f - (v - minV) / range)
                            drawCircle(color, radius = 4f, center = Offset(x, y))
                        }
                    }
                }

                drawAnimated(values, lineColor)
                if (secondary.size == values.size) {
                    drawAnimated(secondary, secondaryLineColor)
                }
            }
        }
    }
}

// ── Correlation strip ─────────────────────────────────────────────────────────
// Compact horizontal strip — keeps each correlation visually cheap so the
// dashboard doesn't degrade into a long list of equally-loud cards.
@Composable
fun CorrelationStripCard(
    item: CorrelationItem,
    modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current

    val (badgeColor, badgeBg) = when {
        item.strength > 0.6f -> ext.signal to ext.signal.copy(alpha = 0.12f)
        item.strength > 0.3f -> ext.data to ext.data.copy(alpha = 0.12f)
        else -> cs.primary to cs.primary.copy(alpha = 0.12f)
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = cs.surface,
        border = BorderStroke(1.dp, ext.gridInk),
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeBg
                ) {
                    Text(
                        text = "${"%.0f".format(item.strength * 100)}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = ext.numerals),
                        color = badgeColor
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurfaceVariant
            )
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────
@Composable
fun DashboardEmptyState(modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val ext = LocalHealthExtended.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = cs.surfaceContainerLow,
        border = BorderStroke(1.dp, ext.gridInk)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.dashboard_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = cs.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.dashboard_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }
    }
}
