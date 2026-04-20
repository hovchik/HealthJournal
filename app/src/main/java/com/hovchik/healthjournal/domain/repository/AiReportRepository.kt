package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.AiReport
import kotlinx.coroutines.flow.Flow

interface AiReportRepository {
    fun getAllReports(): Flow<List<AiReport>>
    suspend fun getReportById(id: Long): AiReport?
    suspend fun insertReport(report: AiReport): Long
    suspend fun deleteReport(report: AiReport)
}
