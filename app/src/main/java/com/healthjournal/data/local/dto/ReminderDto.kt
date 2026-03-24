package com.healthjournal.data.local.dto

import com.healthjournal.domain.model.Reminder
import com.healthjournal.domain.model.ReminderFrequency
import com.healthjournal.domain.model.ReminderType
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Serializable
data class ReminderDto(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val type: String = "CUSTOM",
    val frequency: String = "DAILY",
    val timeHour: Int = 9,
    val timeMinute: Int = 0,
    val enabled: Boolean = true,
    val medicationId: Long = 0,
    val appointmentId: Long = 0,
    val profileId: Long = 0,
    val createdAt: Long = 0,
    val lastTriggeredAt: Long? = null,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
) {
    fun toDomain() = Reminder(
        id = id,
        title = title,
        description = description,
        type = ReminderType.valueOf(type),
        frequency = ReminderFrequency.valueOf(frequency),
        time = LocalTime.of(timeHour, timeMinute),
        enabled = enabled,
        medicationId = medicationId,
        appointmentId = appointmentId,
        profileId = profileId,
        createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
        lastTriggeredAt = lastTriggeredAt?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault()) },
        daysOfWeek = daysOfWeek
    )

    companion object {
        fun from(r: Reminder) = ReminderDto(
            id = r.id,
            title = r.title,
            description = r.description,
            type = r.type.name,
            frequency = r.frequency.name,
            timeHour = r.time.hour,
            timeMinute = r.time.minute,
            enabled = r.enabled,
            medicationId = r.medicationId,
            appointmentId = r.appointmentId,
            profileId = r.profileId,
            createdAt = r.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            lastTriggeredAt = r.lastTriggeredAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
            daysOfWeek = r.daysOfWeek
        )
    }
}
