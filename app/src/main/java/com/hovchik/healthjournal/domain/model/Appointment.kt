package com.hovchik.healthjournal.domain.model

import java.time.LocalDateTime

data class Appointment(
    val id: Long = 0,
    val doctorName: String,
    val specialty: String = "",
    val location: String = "",
    val dateTime: LocalDateTime,
    val notes: String = "",
    val profileId: Long = 0,
    val reminderEnabled: Boolean = true,
    val completed: Boolean = false,
    val preVisitReportGenerated: Boolean = false
)

data class DoctorContact(
    val id: Long = 0,
    val name: String,
    val specialty: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val profileId: Long = 0
)
