package com.hovchik.healthjournal.data.local.dto

import com.hovchik.healthjournal.domain.model.Appointment
import com.hovchik.healthjournal.domain.model.DoctorContact
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Serializable
data class AppointmentDto(
    val id: Long = 0,
    val doctorName: String,
    val specialty: String = "",
    val location: String = "",
    val dateTime: Long = 0,
    val notes: String = "",
    val profileId: Long = 0,
    val reminderEnabled: Boolean = true,
    val completed: Boolean = false,
    val preVisitReportGenerated: Boolean = false
) {
    fun toDomain() = Appointment(
        id = id,
        doctorName = doctorName,
        specialty = specialty,
        location = location,
        dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(dateTime), ZoneId.systemDefault()),
        notes = notes,
        profileId = profileId,
        reminderEnabled = reminderEnabled,
        completed = completed,
        preVisitReportGenerated = preVisitReportGenerated
    )

    companion object {
        fun from(a: Appointment) = AppointmentDto(
            id = a.id,
            doctorName = a.doctorName,
            specialty = a.specialty,
            location = a.location,
            dateTime = a.dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            notes = a.notes,
            profileId = a.profileId,
            reminderEnabled = a.reminderEnabled,
            completed = a.completed,
            preVisitReportGenerated = a.preVisitReportGenerated
        )
    }
}

@Serializable
data class DoctorContactDto(
    val id: Long = 0,
    val name: String,
    val specialty: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val profileId: Long = 0
) {
    fun toDomain() = DoctorContact(
        id = id, name = name, specialty = specialty, phone = phone,
        email = email, address = address, notes = notes, profileId = profileId
    )

    companion object {
        fun from(d: DoctorContact) = DoctorContactDto(
            id = d.id, name = d.name, specialty = d.specialty, phone = d.phone,
            email = d.email, address = d.address, notes = d.notes, profileId = d.profileId
        )
    }
}
