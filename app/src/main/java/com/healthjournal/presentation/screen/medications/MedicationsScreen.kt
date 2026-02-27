package com.healthjournal.presentation.screen.medications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.Medication
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationsScreen(
    onAddMedication: () -> Unit,
    viewModel: MedicationsViewModel = viewModel()
) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.medications_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedication) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medication_desc))
            }
        }
    ) { padding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_medications_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(medications, key = { it.id }) { medication ->
                    MedicationCard(
                        medication = medication,
                        onLogTaken = { viewModel.logTaken(medication) },
                        onToggleActive = { viewModel.toggleMedicationActive(medication) },
                        onDelete = { viewModel.removeMedication(medication) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onLogTaken: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (medication.active) CardDefaults.cardColors()
        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(medication.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${medication.dosage} — ${medication.frequency}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.since_date, medication.startDate.format(dateFormatter)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    if (!medication.active) {
                        Text(
                            stringResource(R.string.inactive),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Row {
                    if (medication.active) {
                        IconButton(onClick = onLogTaken) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(R.string.taken_desc),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
