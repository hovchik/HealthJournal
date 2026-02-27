package com.healthjournal.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.presentation.components.ChartDataPoint
import com.healthjournal.presentation.components.drawChartToCanvas
import java.io.File
import java.time.format.DateTimeFormatter

object PdfReportGenerator {

    fun generateAndShare(
        context: Context,
        report: AiReport,
        vitals: List<VitalSign>,
        memberName: String
    ) {
        val file = generatePdf(context, report, vitals, memberName)
        shareFile(context, file)
    }

    fun generatePdf(
        context: Context,
        report: AiReport,
        vitals: List<VitalSign>,
        memberName: String
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // A4 width in points (72 dpi)
        val pageHeight = 842 // A4 height

        val titlePaint = Paint().apply {
            textSize = 20f
            isFakeBoldText = true
            color = android.graphics.Color.rgb(27, 107, 77)
        }
        val subtitlePaint = Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = android.graphics.Color.DKGRAY
        }
        val bodyPaint = Paint().apply {
            textSize = 11f
            color = android.graphics.Color.BLACK
        }
        val metaPaint = Paint().apply {
            textSize = 10f
            color = android.graphics.Color.GRAY
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val margin = 40f
        val usableWidth = pageWidth - 2 * margin

        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var y = margin + 30f

        // Title
        canvas.drawText("Health Journal Report", margin, y, titlePaint)
        y += 28f

        // Meta info
        canvas.drawText("Profile: $memberName", margin, y, subtitlePaint)
        y += 20f
        canvas.drawText(
            "Generated: ${report.generatedAt.format(dateFormatter)} | Period: ${report.periodDays} days",
            margin, y, metaPaint
        )
        y += 30f

        // Divider
        canvas.drawLine(margin, y, pageWidth - margin, y, metaPaint)
        y += 20f

        // Report content - word-wrap text
        val lines = wrapText(report.content, bodyPaint, usableWidth)
        for (line in lines) {
            if (y > pageHeight - margin - 20) {
                document.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin
            }
            canvas.drawText(line, margin, y, bodyPaint)
            y += 16f
        }

        // Charts section
        val vitalsByType = vitals.groupBy { it.type }
        if (vitalsByType.isNotEmpty()) {
            y += 20f
            if (y > pageHeight - 250) {
                document.finishPage(page)
                pageNum++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = margin
            }

            canvas.drawText("Vital Signs Charts", margin, y, subtitlePaint)
            y += 20f

            val chartDateFormatter = DateTimeFormatter.ofPattern("dd.MM")
            for ((type, typeVitals) in vitalsByType) {
                if (typeVitals.size < 2) continue

                if (y > pageHeight - 200) {
                    document.finishPage(page)
                    pageNum++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }

                canvas.drawText(type.displayName, margin, y, subtitlePaint)
                y += 10f

                val dataPoints = typeVitals.sortedBy { it.recordedAt }.map { v ->
                    ChartDataPoint(
                        label = v.recordedAt.format(chartDateFormatter),
                        value = v.value.toFloat(),
                        secondaryValue = v.secondaryValue?.toFloat()
                    )
                }

                val chartHeight = 150
                canvas.save()
                canvas.translate(margin, y)
                drawChartToCanvas(canvas, (usableWidth).toInt(), chartHeight, dataPoints)
                canvas.restore()
                y += chartHeight + 30f
            }
        }

        document.finishPage(page)

        val dir = File(context.cacheDir, "reports")
        dir.mkdirs()
        val timestamp = System.currentTimeMillis()
        val file = File(dir, "health_report_$timestamp.pdf")
        file.outputStream().use { document.writeTo(it) }
        document.close()

        return file
    }

    private fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            if (paragraph.isBlank()) {
                result.add("")
                continue
            }
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (currentLine.isNotEmpty()) result.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    currentLine = StringBuilder(testLine)
                }
            }
            if (currentLine.isNotEmpty()) result.add(currentLine.toString())
        }
        return result
    }
}
