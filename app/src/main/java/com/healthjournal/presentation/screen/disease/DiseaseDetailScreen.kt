package com.healthjournal.presentation.screen.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.*
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseDetailScreen(
    diseaseId: Long,
    onBack: () -> Unit,
    onAddSymptom: (Long) -> Unit,
    onAddVital: (Long) -> Unit,
    onAddMedication: (Long) -> Unit,
    onEditSymptom: (Long) -> Unit = {},
    onEditVital: (Long) -> Unit = {},
    onEditMedication: (Long) -> Unit = {},
    viewModel: DiseaseViewModel = viewModel()
) {
    val allSymptoms by viewModel.allSymptoms.collectAsStateWithLifecycle()
    val allVitals by viewModel.allVitals.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()

    var disease by remember { mutableStateOf<Disease?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(diseaseId) {
        disease = viewModel.getDiseaseById(diseaseId)
    }

    val symptoms = allSymptoms.filter { it.diseaseId == diseaseId }
    val vitals = allVitals.filter { it.diseaseId == diseaseId }
    val medications = allMedications.filter { it.diseaseId == diseaseId }

    val currentDisease = disease ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentDisease.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!currentDisease.active) {
                            Text(
                                stringResource(R.string.disease_resolved),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit))
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { onAddMedication(diseaseId) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.Medication, contentDescription = stringResource(R.string.add_medication_desc), modifier = Modifier.size(20.dp))
                }
                SmallFloatingActionButton(
                    onClick = { onAddVital(diseaseId) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.add_vital_desc), modifier = Modifier.size(20.dp))
                }
                FloatingActionButton(
                    onClick = { onAddSymptom(diseaseId) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_symptom_desc))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (currentDisease.notes.isNotBlank()) {
                item(key = "notes") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            currentDisease.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Symptoms section
            item(key = "symptoms_header") {
                SectionHeader(
                    icon = Icons.Default.Sick,
                    title = stringResource(R.string.symptoms_title),
                    count = symptoms.size,
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            if (symptoms.isEmpty()) {
                item(key = "empty_symptoms") {
                    EmptyHint(stringResource(R.string.no_symptoms_hint))
                }
            } else {
                items(symptoms.sortedByDescending { it.recordedAt }, key = { "s_${it.id}" }) { symptom ->
                    SymptomMiniCard(symptom, onClick = { onEditSymptom(symptom.id) })
                }
            }

            // Vitals section
            item(key = "vitals_header") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    icon = Icons.Default.MonitorHeart,
                    title = stringResource(R.string.nav_vitals),
                    count = vitals.size,
                    color = MaterialTheme.colorScheme.tertiary,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
            if (vitals.isEmpty()) {
                item(key = "empty_vitals") {
                    EmptyHint(stringResource(R.string.no_vitals_hint))
                }
            } else {
                items(vitals.sortedByDescending { it.recordedAt }, key = { "v_${it.id}" }) { vital ->
                    VitalMiniCard(vital, onClick = { onEditVital(vital.id) })
                }
            }

            // Medications section
            item(key = "meds_header") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(
                    icon = Icons.Default.Medication,
                    title = stringResource(R.string.nav_medications),
                    count = medications.size,
                    color = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
            if (medications.isEmpty()) {
                item(key = "empty_meds") {
                    EmptyHint(stringResource(R.string.no_medications_hint))
                }
            } else {
                items(medications, key = { "m_${it.id}" }) { medication ->
                    MedicationMiniCard(medication, onClick = { onEditMedication(medication.id) })
                }
            }

            item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showEditDialog) {
        EditDiseaseDialog(
            disease = currentDisease,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, notes, active ->
                val updated = currentDisease.copy(name = name, notes = notes, active = active)
                viewModel.updateDisease(updated)
                disease = updated
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(shape = CircleShape, color = containerColor, modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = color)
        Surface(shape = CircleShape, color = color.copy(alpha = 0.12f)) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 42.dp, bottom = 4.dp)
    )
}

@Composable
private fun SymptomMiniCard(symptom: Symptom, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM HH:mm") }
    val intensityColor = when {
        symptom.intensity <= 3 -> MaterialTheme.colorScheme.primary
        symptom.intensity <= 6 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(3.dp).fillMaxHeight().clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)).background(intensityColor))
            Row(modifier = Modifier.weight(1f).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(36.dp), shape = MaterialTheme.shapes.small, color = intensityColor.copy(alpha = 0.1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("${symptom.intensity}", style = MaterialTheme.typography.titleSmall, color = intensityColor, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(symptom.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(symptom.recordedAt.format(formatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
private fun VitalMiniCard(vital: VitalSign, onClick: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM HH:mm") }
    val valueStr = if (vital.secondaryValue != null) "${vital.value.toInt()}/${vital.secondaryValue.toInt()}" else vital.value.toString()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.MonitorHeart, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                Column {
                    Text(vital.type.localizedDisplayName(), style = MaterialTheme.typography.titleSmall)
                    Text(vital.recordedAt.format(formatter), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text("$valueStr ${vital.type.localizedUnit()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun MedicationMiniCard(medication: Medication, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${medication.dosage} — ${medication.frequency}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!medication.active) {
                Surface(shape = MaterialTheme.shapes.extraSmall, color = MaterialTheme.colorScheme.errorContainer) {
                    Text(stringResource(R.string.inactive), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
private fun EditDiseaseDialog(
    disease: Disease,
    onDismiss: () -> Unit,
    onConfirm: (name: String, notes: String, active: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(disease.name) }
    var notes by remember { mutableStateOf(disease.notes) }
    var active by remember { mutableStateOf(disease.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_disease)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.disease_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.disease_active), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, notes, active) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}
