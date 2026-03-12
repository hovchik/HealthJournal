package com.healthjournal.presentation.screen.medications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.util.PredefinedData
import com.healthjournal.util.PredefinedDataKeys
import com.healthjournal.util.predefinedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    medicationId: Long = -1L,
    diseaseId: Long = 0L,
    viewModel: MedicationsViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(medicationId == -1L) }

    // Date state
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isEditing = medicationId != -1L
    var editingMedication by remember { mutableStateOf<com.healthjournal.domain.model.Medication?>(null) }

    LaunchedEffect(medicationId) {
        if (medicationId != -1L) {
            val medication = viewModel.getMedicationById(medicationId)
            if (medication != null) {
                editingMedication = medication
                name = medication.name
                dosage = medication.dosage
                frequency = medication.frequency
                notes = medication.notes
                selectedDate = medication.startDate
            }
            loaded = true
        }
    }

    val disabledMedications by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.DISABLED_MEDICATIONS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())
    val customMedications by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())

    val allMedicationLabels = remember(disabledMedications, customMedications) {
        val predefined = PredefinedData.medications.filter { it.key !in disabledMedications }
        predefined.map { it.key to it.nameResId } to customMedications.toList()
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { onBack() }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    if (!loaded) return

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.close)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        tonalElevation = 1.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}
            }

            // Header row with title and close button
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(if (isEditing) R.string.edit_medication_title else R.string.add_medication_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }

            HorizontalDivider()

            // Scrollable content
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start date picker
                OutlinedTextField(
                    value = selectedDate.format(dateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.start_date_label)) },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    singleLine = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Medication name dropdown with search
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; dropdownExpanded = true },
                        label = { Text(stringResource(R.string.medication_name)) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Medication, contentDescription = null,
                                modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                    )
                    val filteredPredefined = allMedicationLabels.first.filter { (_, resId) ->
                        name.isBlank() || context.getString(resId).contains(name, ignoreCase = true)
                    }
                    val filteredCustom = allMedicationLabels.second.filter {
                        name.isBlank() || it.contains(name, ignoreCase = true)
                    }
                    if (filteredPredefined.isNotEmpty() || filteredCustom.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            filteredPredefined.forEach { (_, resId) ->
                                val label = stringResource(resId)
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { name = label; dropdownExpanded = false }
                                )
                            }
                            filteredCustom.forEach { custom ->
                                DropdownMenuItem(
                                    text = { Text(custom) },
                                    onClick = { name = custom; dropdownExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Dosage & Frequency in a card
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = dosage,
                            onValueChange = { dosage = it },
                            label = { Text(stringResource(R.string.dosage_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = frequency,
                            onValueChange = { frequency = it },
                            label = { Text(stringResource(R.string.schedule_hint)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Save button pinned at bottom
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (name.isNotBlank() && dosage.isNotBlank()) {
                            if (isEditing && editingMedication != null) {
                                viewModel.updateMedication(
                                    editingMedication!!.copy(
                                        name = name,
                                        dosage = dosage,
                                        frequency = frequency,
                                        notes = notes,
                                        startDate = selectedDate
                                    )
                                )
                            } else {
                                viewModel.addNewMedication(name, dosage, frequency, notes, diseaseId = diseaseId, startDate = selectedDate)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    enabled = name.isNotBlank() && dosage.isNotBlank()
                ) {
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
