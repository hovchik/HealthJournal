package com.hovchik.healthjournal.data.local.dto

import com.hovchik.healthjournal.domain.model.AuditAction
import com.hovchik.healthjournal.domain.model.AuditLogEntry
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Serializable
data class AuditLogDto(
    val id: Long = 0,
    val action: String,
    val details: String = "",
    val timestamp: Long = 0,
    val profileId: Long = 0
) {
    fun toDomain() = AuditLogEntry(
        id = id,
        action = AuditAction.valueOf(action),
        details = details,
        timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()),
        profileId = profileId
    )

    companion object {
        fun from(e: AuditLogEntry) = AuditLogDto(
            id = e.id,
            action = e.action.name,
            details = e.details,
            timestamp = e.timestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            profileId = e.profileId
        )
    }
}
