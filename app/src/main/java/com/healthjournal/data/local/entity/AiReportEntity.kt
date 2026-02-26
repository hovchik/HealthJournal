package com.healthjournal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_reports")
data class AiReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val type: String, // ReportType enum name
    val periodDays: Int,
    val generatedAt: Long // epoch millis
)
