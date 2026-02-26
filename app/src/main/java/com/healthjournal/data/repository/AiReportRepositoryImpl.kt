package com.healthjournal.data.repository

import com.healthjournal.data.local.dao.AiReportDao
import com.healthjournal.domain.model.AiReport
import com.healthjournal.domain.repository.AiReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiReportRepositoryImpl @Inject constructor(
    private val dao: AiReportDao
) : AiReportRepository {

    override fun getAllReports(): Flow<List<AiReport>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getReportById(id: Long): AiReport? = dao.getById(id)?.toDomain()

    override suspend fun insertReport(report: AiReport): Long = dao.insert(report.toEntity())

    override suspend fun deleteReport(report: AiReport) = dao.delete(report.toEntity())
}
