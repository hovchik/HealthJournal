package com.healthjournal.presentation.screen.appointments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Appointment
import com.healthjournal.domain.model.DoctorContact
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AppointmentsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val appointmentRepo = container.appointmentRepository

    val appointments = appointmentRepo.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingAppointments = appointmentRepo.getUpcomingAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctors = appointmentRepo.getAllDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAppointment(
        doctorName: String,
        specialty: String,
        location: String,
        dateTime: LocalDateTime,
        notes: String
    ) {
        viewModelScope.launch {
            appointmentRepo.insertAppointment(
                Appointment(
                    doctorName = doctorName,
                    specialty = specialty,
                    location = location,
                    dateTime = dateTime,
                    notes = notes
                )
            )
        }
    }

    fun markCompleted(appointment: Appointment) {
        viewModelScope.launch {
            appointmentRepo.updateAppointment(appointment.copy(completed = true))
        }
    }

    fun deleteAppointment(appointment: Appointment) {
        viewModelScope.launch {
            appointmentRepo.deleteAppointment(appointment)
        }
    }

    fun addDoctor(name: String, specialty: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            appointmentRepo.insertDoctor(
                DoctorContact(
                    name = name, specialty = specialty,
                    phone = phone, email = email, address = address
                )
            )
        }
    }

    fun deleteDoctor(doctor: DoctorContact) {
        viewModelScope.launch {
            appointmentRepo.deleteDoctor(doctor)
        }
    }
}
