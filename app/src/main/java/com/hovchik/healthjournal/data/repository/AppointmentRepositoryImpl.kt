package com.hovchik.healthjournal.data.repository

import com.hovchik.healthjournal.data.local.JsonFileStore
import com.hovchik.healthjournal.data.local.dto.AppointmentDto
import com.hovchik.healthjournal.data.local.dto.DoctorContactDto
import com.hovchik.healthjournal.domain.model.Appointment
import com.hovchik.healthjournal.domain.model.DoctorContact
import com.hovchik.healthjournal.domain.repository.AppointmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class AppointmentRepositoryImpl(
    private val appointmentStore: JsonFileStore<AppointmentDto>,
    private val doctorStore: JsonFileStore<DoctorContactDto>
) : AppointmentRepository {

    override fun getAllAppointments(): Flow<List<Appointment>> =
        appointmentStore.observeAll().map { list -> list.map { it.toDomain() }.sortedBy { it.dateTime } }

    override fun getUpcomingAppointments(): Flow<List<Appointment>> =
        appointmentStore.observeAll().map { list ->
            val now = LocalDateTime.now()
            list.map { it.toDomain() }
                .filter { it.dateTime.isAfter(now) && !it.completed }
                .sortedBy { it.dateTime }
        }

    override suspend fun getAppointmentById(id: Long): Appointment? =
        appointmentStore.getById(id)?.toDomain()

    override suspend fun insertAppointment(appointment: Appointment): Long =
        appointmentStore.insert(AppointmentDto.from(appointment))

    override suspend fun updateAppointment(appointment: Appointment) =
        appointmentStore.update(AppointmentDto.from(appointment))

    override suspend fun deleteAppointment(appointment: Appointment) =
        appointmentStore.delete(AppointmentDto.from(appointment))

    override fun getAllDoctors(): Flow<List<DoctorContact>> =
        doctorStore.observeAll().map { list -> list.map { it.toDomain() }.sortedBy { it.name } }

    override suspend fun insertDoctor(doctor: DoctorContact): Long =
        doctorStore.insert(DoctorContactDto.from(doctor))

    override suspend fun updateDoctor(doctor: DoctorContact) =
        doctorStore.update(DoctorContactDto.from(doctor))

    override suspend fun deleteDoctor(doctor: DoctorContact) =
        doctorStore.delete(DoctorContactDto.from(doctor))
}
