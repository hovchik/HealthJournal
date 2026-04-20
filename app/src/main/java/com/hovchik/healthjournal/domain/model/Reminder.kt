package com.hovchik.healthjournal.domain.model

import java.time.LocalDateTime
import java.time.LocalTime

enum class ReminderType {
    MEDICATION,
    SYMPTOM_CHECK,
    VITAL_CHECK,
    APPOINTMENT,
    CUSTOM
}

enum class ReminderFrequency {
    ONCE,
    DAILY,
    WEEKLY,
    MONTHLY
}

data class Reminder(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: ReminderType = ReminderType.CUSTOM,
    val frequency: ReminderFrequency = ReminderFrequency.DAILY,
    val time: LocalTime = LocalTime.of(9, 0),
    val enabled: Boolean = true,
    val medicationId: Long = 0,
    val appointmentId: Long = 0,
    val profileId: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastTriggeredAt: LocalDateTime? = null,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
)
