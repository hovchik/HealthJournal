package com.healthjournal.presentation.screen.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.domain.model.Appointment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    viewModel: AppointmentsViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddDoctorDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appointments_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (selectedTab == 0) showAddDialog = true else showAddDoctorDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        if (selectedTab == 0) stringResource(R.string.add_appointment)
                        else stringResource(R.string.add_doctor)
                    )
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.appointments_tab)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.doctors_tab)) })
            }

            when (selectedTab) {
                0 -> AppointmentsList(
                    appointments = appointments,
                    onComplete = { viewModel.markCompleted(it) },
                    onDelete = { viewModel.deleteAppointment(it) }
                )
                1 -> DoctorsList(
                    doctors = doctors,
                    onDelete = { viewModel.deleteDoctor(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddAppointmentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, specialty, location, date, notes ->
                viewModel.addAppointment(name, specialty, location, date, notes)
                showAddDialog = false
            }
        )
    }

    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onConfirm = { name, specialty, phone, email, address ->
                viewModel.addDoctor(name, specialty, phone, email, address)
                showAddDoctorDialog = false
            }
        )
    }
}

@Composable
private fun AppointmentsList(
    appointments: List<Appointment>,
    onComplete: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }

    if (appointments.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_appointments_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(appointments, key = { it.id }) { appointment ->
                ElevatedCard(shape = MaterialTheme.shapes.medium) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (appointment.completed) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (appointment.completed) Icons.Default.CheckCircle else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (appointment.completed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(appointment.doctorName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            if (appointment.specialty.isNotBlank()) {
                                Text(appointment.specialty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                appointment.dateTime.format(dateFormatter),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            if (appointment.location.isNotBlank()) {
                                Text(appointment.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (!appointment.completed) {
                            IconButton(onClick = { onComplete(appointment) }) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        IconButton(onClick = { onDelete(appointment) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DoctorsList(
    doctors: List<com.healthjournal.domain.model.DoctorContact>,
    onDelete: (com.healthjournal.domain.model.DoctorContact) -> Unit
) {
    if (doctors.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_doctors_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(doctors, key = { it.id }) { doctor ->
                ElevatedCard(shape = MaterialTheme.shapes.medium) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doctor.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            if (doctor.specialty.isNotBlank()) Text(doctor.specialty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            if (doctor.phone.isNotBlank()) Text(doctor.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDelete(doctor) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAppointmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, LocalDateTime, String) -> Unit
) {
    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dateStr by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var timeStr by remember { mutableStateOf("10:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_appointment)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = doctorName, onValueChange = { doctorName = it },
                    label = { Text(stringResource(R.string.doctor_name)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialty, onValueChange = { specialty = it },
                    label = { Text(stringResource(R.string.specialty)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = location, onValueChange = { location = it },
                    label = { Text(stringResource(R.string.location)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = dateStr, onValueChange = { dateStr = it },
                    label = { Text(stringResource(R.string.date_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = timeStr, onValueChange = { timeStr = it },
                    label = { Text(stringResource(R.string.time_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (doctorName.isNotBlank()) {
                    val date = try { LocalDate.parse(dateStr) } catch (_: Exception) { LocalDate.now().plusDays(1) }
                    val time = try {
                        val parts = timeStr.split(":")
                        LocalTime.of(parts[0].toInt(), parts[1].toInt())
                    } catch (_: Exception) { LocalTime.of(10, 0) }
                    onConfirm(doctorName, specialty, location, LocalDateTime.of(date, time), notes)
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun AddDoctorDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_doctor)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text(stringResource(R.string.doctor_name)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = specialty, onValueChange = { specialty = it },
                    label = { Text(stringResource(R.string.specialty)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text(stringResource(R.string.doctor_phone)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text(stringResource(R.string.address_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onConfirm(name, specialty, phone, email, address)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
