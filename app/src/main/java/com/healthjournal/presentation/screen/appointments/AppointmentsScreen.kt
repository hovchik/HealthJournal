package com.healthjournal.presentation.screen.appointments

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.domain.model.Appointment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    onBack: () -> Unit,
    isTopLevel: Boolean = false,
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
                    if (!isTopLevel) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 1) showAddDoctorDialog = true
                    else showAddDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(
                    if (selectedTab == 1) R.string.add_doctor else R.string.add_appointment
                ))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.calendar_tab)) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.doctors_tab)) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    text = { Text(stringResource(R.string.appointments_tab)) },
                    icon = { Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(18.dp)) })
            }

            when (selectedTab) {
                0 -> CalendarView(
                    appointments = appointments,
                    onComplete = { viewModel.markCompleted(it) },
                    onDelete = { viewModel.deleteAppointment(it) }
                )
                1 -> DoctorsList(
                    doctors = doctors,
                    onDelete = { viewModel.deleteDoctor(it) }
                )
                2 -> AppointmentsList(
                    appointments = appointments,
                    onComplete = { viewModel.markCompleted(it) },
                    onDelete = { viewModel.deleteAppointment(it) }
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
private fun CalendarView(
    appointments: List<Appointment>,
    onComplete: (Appointment) -> Unit,
    onDelete: (Appointment) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val appointmentDays = remember(appointments, currentMonth) {
        appointments
            .filter { YearMonth.from(it.dateTime.toLocalDate()) == currentMonth }
            .map { it.dateTime.toLocalDate() }
            .toSet()
    }

    val selectedDayAppointments = remember(appointments, selectedDate) {
        appointments.filter { it.dateTime.toLocalDate() == selectedDate }
            .sortedBy { it.dateTime }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        // Month navigation header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = null)
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }

        // Day of week headers
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val daysOfWeek = listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                )
                daysOfWeek.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Calendar grid
        item {
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                appointmentDays = appointmentDays,
                onDateSelected = { selectedDate = it }
            )
        }

        // Selected day appointments
        item {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (selectedDayAppointments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.no_appointments_for_day),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(selectedDayAppointments, key = { it.id }) { appointment ->
                ElevatedCard(
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (appointment.completed) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (appointment.completed) Icons.Default.CheckCircle else Icons.Default.Event,
                                    contentDescription = null,
                                    tint = if (appointment.completed) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
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
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        IconButton(onClick = { onDelete(appointment) }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    appointmentDays: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    // Monday = 1, Sunday = 7
    val startOffset = (firstDayOfMonth.dayOfWeek.value - 1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = LocalDate.now()

    // Create list of day cells (nulls for empty cells, date for days)
    val cells = buildList {
        repeat(startOffset) { add(null) }
        for (day in 1..daysInMonth) {
            add(yearMonth.atDay(day))
        }
    }

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isToday = date == today
                            val hasAppointment = date in appointmentDays

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = date.dayOfMonth.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (hasAppointment) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.tertiary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Fill remaining cells in the last row
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
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
