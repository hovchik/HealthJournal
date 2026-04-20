package com.hovchik.healthjournal.domain.model

import java.time.LocalDateTime

data class MedicationLog(
    val id: Long = 0,
    val medicationId: Long,
    val taken: Boolean = true,
    val takenAt: LocalDateTime = LocalDateTime.now(),
    val notes: String = ""
)
