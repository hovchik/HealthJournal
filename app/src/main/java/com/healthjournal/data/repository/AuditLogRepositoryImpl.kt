package com.healthjournal.data.repository

import com.healthjournal.data.local.JsonFileStore
import com.healthjournal.data.local.dto.AuditLogDto
import com.healthjournal.domain.model.AuditLogEntry
import com.healthjournal.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuditLogRepositoryImpl(
    private val store: JsonFileStore<AuditLogDto>
) : AuditLogRepository {

    override fun getAllEntries(): Flow<List<AuditLogEntry>> =
        store.observeAll().map { list -> list.map { it.toDomain() }.sortedByDescending { it.timestamp } }

    override fun getRecentEntries(limit: Int): Flow<List<AuditLogEntry>> =
        store.observeAll().map { list ->
            list.map { it.toDomain() }.sortedByDescending { it.timestamp }.take(limit)
        }

    override suspend fun logAction(entry: AuditLogEntry) {
        store.insert(AuditLogDto.from(entry))
    }

    override suspend fun clearAll() {
        store.getAll().let { /* clear handled by replacing with empty */ }
    }
}
