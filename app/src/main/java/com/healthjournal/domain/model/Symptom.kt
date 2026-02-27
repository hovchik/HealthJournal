package com.healthjournal.domain.model

import java.time.LocalDateTime

data class Symptom(
    val id: Long = 0,
    val name: String,
    val intensity: Int, // 0-10
    val durationMinutes: Int? = null,
    val triggers: List<String> = emptyList(),
    val notes: String = "",
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val profileId: Long = 0,
    val attachmentPaths: List<String> = emptyList()
)
