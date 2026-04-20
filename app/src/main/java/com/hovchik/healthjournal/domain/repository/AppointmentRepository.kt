package com.hovchik.healthjournal.domain.repository

import com.hovchik.healthjournal.domain.model.Appointment
import com.hovchik.healthjournal.domain.model.DoctorContact
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
    fun getAllAppointments(): Flow<List<Appointment>>
    fun getUpcomingAppointments(): Flow<List<Appointment>>
    suspend fun getAppointmentById(id: Long): Appointment?
    suspend fun insertAppointment(appointment: Appointment): Long
    suspend fun updateAppointment(appointment: Appointment)
    suspend fun deleteAppointment(appointment: Appointment)

    fun getAllDoctors(): Flow<List<DoctorContact>>
    suspend fun insertDoctor(doctor: DoctorContact): Long
    suspend fun updateDoctor(doctor: DoctorContact)
    suspend fun deleteDoctor(doctor: DoctorContact)
}
