package com.hovchik.healthjournal.presentation.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.util.AttachmentHelper
import com.hovchik.healthjournal.util.PredefinedData
import com.hovchik.healthjournal.util.PredefinedDataKeys
import com.hovchik.healthjournal.util.predefinedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSymptomScreen(
    onBack: () -> Unit,
    symptomId: Long = -1L,
    diseaseId: Long = 0L,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(5f) }
    var value by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var attachmentPaths by remember { mutableStateOf(listOf<String>()) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(symptomId == -1L) }

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isEditing = symptomId != -1L
    var editingSymptom by remember { mutableStateOf<com.hovchik.healthjournal.domain.model.Symptom?>(null) }

    LaunchedEffect(symptomId) {
        if (symptomId != -1L) {
            val symptom = viewModel.getSymptomById(symptomId)
            if (symptom != null) {
                editingSymptom = symptom
                name = symptom.name
                intensity = symptom.intensity.toFloat()
                value = symptom.value ?: ""
                duration = symptom.durationMinutes?.toString() ?: ""
                triggers = symptom.triggers.joinToString(", ")
                notes = symptom.notes
                attachmentPaths = symptom.attachmentPaths
                selectedDate = symptom.recordedAt.toLocalDate()
                selectedTime = symptom.recordedAt.toLocalTime()
            }
            loaded = true
        }
    }

    val disabledSymptoms by remember {
        context.predefinedDataStore.data.map { prefs -> prefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet() }
    }.collectAsState(initial = emptySet())
    val customSymptoms by remember {
        context.predefinedDataStore.data.map { prefs -> prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet() }
    }.collectAsState(initial = emptySet())

    val allSymptomLabels = remember(disabledSymptoms, customSymptoms) {
        val predefined = PredefinedData.symptoms.filter { it.key !in disabledSymptoms }
        predefined.map { it.key to it.nameResId } to customSymptoms.toList()
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        val paths = uris.mapNotNull { uri -> AttachmentHelper.copyToInternal(context, uri) }
        attachmentPaths = attachmentPaths + paths
    }

    LaunchedEffect(Unit) { viewModel.saveSuccess.collectLatest { onBack() } }

    val intensityColor by animateColorAsState(
        when {
            intensity.toInt() <= 3 -> MaterialTheme.colorScheme.primary
            intensity.toInt() <= 6 -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.error
        },
        label = "intensityColor"
    )

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

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

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.close)) }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (isEditing) R.string.edit_symptom_title else R.string.add_symptom_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val triggerList = triggers.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val recordedAt = LocalDateTime.of(selectedDate, selectedTime)
                            val currentSymptom = editingSymptom
                            if (isEditing && currentSymptom != null) {
                                viewModel.updateSymptom(
                                    currentSymptom.copy(
                                        name = name,
                                        intensity = intensity.toInt(),
                                        value = value.takeIf { it.isNotBlank() },
                                        durationMinutes = duration.toIntOrNull(),
                                        triggers = triggerList,
                                        notes = notes,
                                        attachmentPaths = attachmentPaths,
                                        recordedAt = recordedAt
                                    )
                                )
                            } else {
                                viewModel.addNewSymptom(
                                    name = name, intensity = intensity.toInt(),
                                    value = value,
                                    durationMinutes = duration.toIntOrNull(),
                                    triggers = triggerList,
                                    notes = notes, attachmentPaths = attachmentPaths,
                                    diseaseId = diseaseId,
                                    recordedAt = recordedAt
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.save), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Date & Time chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string.date_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showTimePicker = true },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                stringResource(R.string.time_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                selectedTime.format(timeFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Symptom name
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; dropdownExpanded = true },
                        label = { Text(stringResource(R.string.symptom_name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        shape = RoundedCornerShape(12.dp)
                    )
                    val filteredPredefined = allSymptomLabels.first.filter { (_, resId) ->
                        name.isBlank() || context.getString(resId).contains(name, ignoreCase = true)
                    }
                    val filteredCustom = allSymptomLabels.second.filter {
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
            }

            // Intensity gauge card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.intensity_label, intensity.toInt()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                when {
                                    intensity.toInt() <= 3 -> "Mild"
                                    intensity.toInt() <= 6 -> "Moderate"
                                    else -> "Severe"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = intensityColor
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = intensityColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${intensity.toInt()}",
                                    color = intensityColor,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Intensity bar
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = intensityColor,
                            activeTrackColor = intensityColor,
                            activeTickColor = intensityColor.copy(alpha = 0.5f),
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            inactiveTickColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    // Intensity step indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Additional fields in a grouped card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text(stringResource(R.string.symptom_value_label)) },
                        placeholder = { Text(stringResource(R.string.symptom_value_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text(stringResource(R.string.duration_minutes)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = triggers,
                        onValueChange = { triggers = it },
                        label = { Text(stringResource(R.string.triggers_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.FlashOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Attachments section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_file))
                }

                attachmentPaths.forEachIndexed { index, path ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                File(path).name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            IconButton(
                                onClick = {
                                    attachmentPaths = attachmentPaths.toMutableList().also { it.removeAt(index) }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
