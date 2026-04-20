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
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
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

private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM")

/*
 * Data blocks. Every block is flat: just text, a canvas where appropriate,
 * and hairlines supplied by the caller. No Surfaces, no borders, no shadows.
 */

// ── Glance row ───────────────────────────────────────────────────────────────
// Two readings side by side. Each occupies two columns of a 5-col layout;
// the remaining column becomes the space between them. Big numerals, serif
// captions stacked underneath — no icons, no containers.
@Composable
fun GlanceRow(
    symptomsCount: Int,
    vitalsCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EditorialMargin, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        GlanceColumn(
            value = symptomsCount.toString(),
            caption = stringResource(R.string.symptoms_title).lowercase(),
            modifier = Modifier.weight(2f)
        )
        GlanceColumn(
            value = vitalsCount.toString(),
            caption = stringResource(R.string.nav_vitals).lowercase(),
            modifier = Modifier.weight(2f)
        )
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun GlanceColumn(
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    Column(modifier = modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = ext.numerals),
            color = ext.ink
        )
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = ext.muted
        )
    }
}

// ── Pulse strip ──────────────────────────────────────────────────────────────
// Heatmap as full-bleed dot field — edge to edge inside the editorial margin.
// Three rows of dots per column; saturation tracks symptom count. Tap a
// column → spring-expand a one-line callout under the strip with date + count.
@Composable
fun PulseStrip(
    symptomsByDay: Map<LocalDate, Int>,
    days: Int,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    val today = remember(days) { LocalDate.now() }
    val maxCount = remember(symptomsByDay) { symptomsByDay.values.maxOrNull() ?: 0 }

    var selectedIndex by remember(days) { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorialMargin)
                .height(84.dp)
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
                val dotRadius = (cellW.coerceAtMost(rowSpacing) * 0.32f).coerceAtLeast(2f)

                for (i in 0 until days) {
                    val date = today.minusDays((days - 1 - i).toLong())
                    val count = symptomsByDay[date] ?: 0
                    val intensity = if (maxCount > 0) {
                        (count.toFloat() / maxCount).coerceIn(0f, 1f)
                    } else 0f

                    for (r in 0 until rows) {
                        val centerX = i * cellW + cellW / 2f
                        val centerY = rowSpacing * (r + 1)
                        val rowBoost = if (r == 1) 1f else 0.5f
                        val alpha = (intensity * rowBoost).coerceIn(0f, 1f)
                        val color = if (alpha < 0.05f) {
                            ext.hairline
                        } else {
                            ext.signal.copy(alpha = 0.25f + alpha * 0.75f)
                        }
                        drawCircle(
                            color = color,
                            radius = dotRadius,
                            center = Offset(centerX, centerY)
                        )
                    }
                }

                // Subtle vertical tick on the selected column — no box, no fill.
                selectedIndex?.let { idx ->
                    val x = idx * cellW + cellW / 2f
                    drawLine(
                        color = ext.ink,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 1f
                    )
                }
            }
        }

        // Dates flanking the strip — muted, tabular, outside the chart
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = EditorialMargin, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = today.minusDays((days - 1).toLong()).format(isoFormatter),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = ext.numerals),
                color = ext.muted
            )
            Text(
                text = today.format(isoFormatter),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = ext.numerals),
                color = ext.muted
            )
        }

        // Inline callout: appears *below* the strip with spring, no enclosing
        // card — just a line of text between two hairlines.
        AnimatedVisibility(
            visible = selectedIndex != null,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
            exit = fadeOut(tween(120)) + shrinkVertically(tween(160))
        ) {
            val idx = selectedIndex ?: 0
            val date = today.minusDays((days - 1 - idx).toLong())
            val count = symptomsByDay[date] ?: 0
            Column {
                HairlineRule()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = EditorialMargin, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date.format(isoFormatter),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = ext.numerals),
                        color = ext.ink
                    )
                    Text(
                        text = stringResource(R.string.dashboard_entries_for, count),
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                        color = if (count > 0) ext.signal else ext.muted
                    )
                }
                HairlineRule()
            }
        }
    }
}

// ── Vital block ──────────────────────────────────────────────────────────────
// The hero of each trend. Vital name (uppercase Grotesque) above a giant
// monospace reading; unit to the right as italic serif; then a full-bleed
// sparkline that animates its stroke in via PathMeasure. Out-of-range
// readings render in signal colour — the only reactive use of color.
@Composable
fun VitalBlock(
    type: VitalType,
    vitals: List<VitalSign>,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current
    val sorted = remember(vitals) { vitals.sortedBy { it.recordedAt } }
    val latest = sorted.last()
    val displayValue = if (latest.secondaryValue != null) {
        "${latest.value.toInt()}/${latest.secondaryValue.toInt()}"
    } else "%.1f".format(latest.value)

    // Naive out-of-range flag — only delta >20% from series median triggers
    // signal colour. Better heuristics live in domain code; this is purely
    // presentational.
    val flagged = remember(sorted) {
        val values = sorted.map { it.value.toFloat() }.sorted()
        val median = values[values.size / 2]
        median > 0f && kotlin.math.abs(latest.value.toFloat() - median) / median > 0.2f
    }

    val drawProgress = remember(vitals) { Animatable(0f) }
    LaunchedEffect(vitals) {
        drawProgress.snapTo(0f)
        drawProgress.animateTo(1f, tween(durationMillis = 620, easing = EaseOutCubic))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(
                start = EditorialMargin,
                end = EditorialMargin,
                top = 16.dp,
                bottom = 8.dp
            )
        ) {
            Text(
                text = type.localizedDisplayName().uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = ext.muted
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = displayValue,
                    style = MaterialTheme.typography.displayLarge.copy(fontFamily = ext.numerals),
                    color = if (flagged) ext.signal else ext.ink
                )
                Text(
                    text = type.localizedUnit(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = ext.muted,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }
        }

        val lineColor = ext.dataB
        val secondaryLineColor = ext.dataA
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = EditorialMargin)
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
                drawPath(visible, color, style = Stroke(width = 1.5f, cap = StrokeCap.Round))

                val fraction = drawProgress.value
                series.forEachIndexed { i, v ->
                    val t = i.toFloat() / (series.size - 1).coerceAtLeast(1)
                    if (t <= fraction) {
                        val x = size.width * i / (series.size - 1).coerceAtLeast(1)
                        val y = size.height * (1f - (v - minV) / range)
                        drawCircle(color, radius = 2.5f, center = Offset(x, y))
                    }
                }
            }

            drawAnimated(values, lineColor)
            if (secondary.size == values.size) {
                drawAnimated(secondary, secondaryLineColor)
            }
        }

        Spacer(Modifier.height(16.dp))
        HairlineRule()
    }
}

// ── Correlation row ──────────────────────────────────────────────────────────
// One line per correlation. Title left, percentage right at the same baseline,
// description in italic serif underneath. Between rows: 0.5dp hairline.
@Composable
fun CorrelationRow(
    item: CorrelationItem,
    modifier: Modifier = Modifier
) {
    val ext = LocalHealthExtended.current

    val strengthColor = when {
        item.strength > 0.6f -> ext.signal
        item.strength > 0.3f -> ext.dataA
        else -> ext.ink
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = EditorialMargin, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                color = ext.ink,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${"%.0f".format(item.strength * 100)}%",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = ext.numerals),
                color = strengthColor
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.description,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = ext.muted
        )
    }
}
