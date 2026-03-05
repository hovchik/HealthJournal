package com.healthjournal.domain.model

import java.time.LocalDateTime

enum class ReportType {
    SUMMARY,
    PATTERN_ANALYSIS
}

data class AiReport(
    val id: Long = 0,
    val content: String,
    val type: ReportType,
    val periodDays: Int = 7,
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val profileId: Long = 0,
    val diseaseId: Long = 0
)
