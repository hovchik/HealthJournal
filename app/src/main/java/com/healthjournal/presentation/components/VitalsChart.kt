package com.healthjournal.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import java.time.format.DateTimeFormatter

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val secondaryValue: Float? = null
)

@Composable
fun VitalsChart(
    title: String,
    vitals: List<VitalSign>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    secondaryLineColor: Color = MaterialTheme.colorScheme.tertiary
) {
    if (vitals.isEmpty()) return

    val formatter = DateTimeFormatter.ofPattern("dd.MM")
    val dataPoints = vitals.sortedBy { it.recordedAt }.map { v ->
        ChartDataPoint(
            label = v.recordedAt.format(formatter),
            value = v.value.toFloat(),
            secondaryValue = v.secondaryValue?.toFloat()
        )
    }

    Column(modifier = modifier) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val primaryColor = lineColor
        val secondaryColor = secondaryLineColor

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            drawChartContent(dataPoints, primaryColor, secondaryColor)
        }
    }
}

private fun DrawScope.drawChartContent(
    dataPoints: List<ChartDataPoint>,
    primaryColor: Color,
    secondaryColor: Color
) {
    if (dataPoints.size < 2) return

    val paddingLeft = 50f
    val paddingRight = 20f
    val paddingTop = 20f
    val paddingBottom = 40f

    val chartWidth = size.width - paddingLeft - paddingRight
    val chartHeight = size.height - paddingTop - paddingBottom

    val allValues = dataPoints.flatMap { listOfNotNull(it.value, it.secondaryValue) }
    val minVal = allValues.min()
    val maxVal = allValues.max()
    val range = (maxVal - minVal).coerceAtLeast(1f)

    // Grid lines
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    for (i in 0..4) {
        val y = paddingTop + chartHeight * i / 4f
        drawLine(gridColor, Offset(paddingLeft, y), Offset(size.width - paddingRight, y))
        // Label
        val labelValue = maxVal - range * i / 4f
        drawContext.canvas.nativeCanvas.drawText(
            "%.0f".format(labelValue),
            5f, y + 5f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.GRAY
                textSize = 24f
            }
        )
    }

    // Draw primary line
    drawDataLine(dataPoints.map { it.value }, minVal, range, chartWidth, chartHeight, paddingLeft, paddingTop, primaryColor)

    // Draw secondary line (e.g., diastolic for blood pressure)
    val secondaryValues = dataPoints.mapNotNull { it.secondaryValue }
    if (secondaryValues.size == dataPoints.size) {
        drawDataLine(secondaryValues, minVal, range, chartWidth, chartHeight, paddingLeft, paddingTop, secondaryColor)
    }

    // X-axis labels
    val labelInterval = (dataPoints.size / 5).coerceAtLeast(1)
    dataPoints.forEachIndexed { index, point ->
        if (index % labelInterval == 0 || index == dataPoints.lastIndex) {
            val x = paddingLeft + chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1)
            drawContext.canvas.nativeCanvas.drawText(
                point.label,
                x - 20f, size.height - 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 22f
                }
            )
        }
    }
}

private fun DrawScope.drawDataLine(
    values: List<Float>,
    minVal: Float,
    range: Float,
    chartWidth: Float,
    chartHeight: Float,
    paddingLeft: Float,
    paddingTop: Float,
    color: Color
) {
    if (values.size < 2) return

    val path = Path()
    values.forEachIndexed { index, value ->
        val x = paddingLeft + chartWidth * index / (values.size - 1).coerceAtLeast(1)
        val y = paddingTop + chartHeight * (1f - (value - minVal) / range)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    drawPath(path, color, style = Stroke(width = 3f, cap = StrokeCap.Round))

    // Draw dots
    values.forEachIndexed { index, value ->
        val x = paddingLeft + chartWidth * index / (values.size - 1).coerceAtLeast(1)
        val y = paddingTop + chartHeight * (1f - (value - minVal) / range)
        drawCircle(color, radius = 5f, center = Offset(x, y))
    }
}

/**
 * Draws chart content to an Android Canvas (for PDF generation).
 */
fun drawChartToCanvas(
    canvas: android.graphics.Canvas,
    width: Int,
    height: Int,
    dataPoints: List<ChartDataPoint>,
    primaryColor: Int = android.graphics.Color.rgb(27, 107, 77),
    secondaryColor: Int = android.graphics.Color.rgb(107, 77, 27)
) {
    if (dataPoints.size < 2) return

    val paddingLeft = 60f
    val paddingRight = 20f
    val paddingTop = 20f
    val paddingBottom = 40f

    val chartWidth = width - paddingLeft - paddingRight
    val chartHeight = height - paddingTop - paddingBottom

    val allValues = dataPoints.flatMap { listOfNotNull(it.value, it.secondaryValue) }
    val minVal = allValues.min()
    val maxVal = allValues.max()
    val range = (maxVal - minVal).coerceAtLeast(1f)

    val gridPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
    }
    val labelPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 20f
    }
    val linePaint = android.graphics.Paint().apply {
        color = primaryColor
        strokeWidth = 3f
        style = android.graphics.Paint.Style.STROKE
        isAntiAlias = true
    }

    // Grid
    for (i in 0..4) {
        val y = paddingTop + chartHeight * i / 4f
        canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
        canvas.drawText("%.0f".format(maxVal - range * i / 4f), 5f, y + 6f, labelPaint)
    }

    // Primary line
    val path = android.graphics.Path()
    dataPoints.forEachIndexed { index, point ->
        val x = paddingLeft + chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1)
        val y = paddingTop + chartHeight * (1f - (point.value - minVal) / range)
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    canvas.drawPath(path, linePaint)

    // Secondary line
    val secondaryValues = dataPoints.mapNotNull { it.secondaryValue }
    if (secondaryValues.size == dataPoints.size) {
        val secPath = android.graphics.Path()
        linePaint.color = secondaryColor
        dataPoints.forEachIndexed { index, point ->
            val x = paddingLeft + chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1)
            val y = paddingTop + chartHeight * (1f - (point.secondaryValue!! - minVal) / range)
            if (index == 0) secPath.moveTo(x, y) else secPath.lineTo(x, y)
        }
        canvas.drawPath(secPath, linePaint)
    }

    // X labels
    val labelInterval = (dataPoints.size / 5).coerceAtLeast(1)
    dataPoints.forEachIndexed { index, point ->
        if (index % labelInterval == 0 || index == dataPoints.lastIndex) {
            val x = paddingLeft + chartWidth * index / (dataPoints.size - 1).coerceAtLeast(1)
            canvas.drawText(point.label, x - 15f, height.toFloat() - 5f, labelPaint)
        }
    }
}
