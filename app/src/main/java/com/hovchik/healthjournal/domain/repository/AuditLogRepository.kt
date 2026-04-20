package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.AuditLogEntry
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAllEntries(): Flow<List<AuditLogEntry>>
    fun getRecentEntries(limit: Int = 50): Flow<List<AuditLogEntry>>
    suspend fun logAction(entry: AuditLogEntry)
    suspend fun clearAll()
}
