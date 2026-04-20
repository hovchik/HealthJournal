package com.hovchik.healthjournal.data.repository

import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.AiReportDto
import com.hovchik.healthjournal.domain.model.AiReport
import com.hovchik.healthjournal.domain.repository.AiReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AiReportRepositoryImpl constructor(
    private val store: JsonFileStore<AiReportDto>
) : AiReportRepository {

    override fun getAllReports(): Flow<List<AiReport>> =
        store.observeAll().map { list ->
            list.sortedByDescending { it.generatedAt }.map { it.toDomain() }
        }

    override suspend fun getReportById(id: Long): AiReport? =
        store.getById(id)?.toDomain()

    override suspend fun insertReport(report: AiReport): Long =
        store.insert(AiReportDto.from(report))

    override suspend fun deleteReport(report: AiReport) =
        store.delete(AiReportDto.from(report))
}
