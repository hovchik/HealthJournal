package com.hovchik.healthjournal.domain.model

import java.time.LocalDateTime

enum class AuditAction {
    DATA_VIEWED,
    DATA_EXPORTED,
    DATA_IMPORTED,
    PROFILE_SWITCHED,
    AI_REPORT_GENERATED,
    SETTINGS_CHANGED,
    APP_UNLOCKED,
    REMINDER_CREATED,
    APPOINTMENT_CREATED
}

data class AuditLogEntry(
    val id: Long = 0,
    val action: AuditAction,
    val details: String = "",
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val profileId: Long = 0
)
