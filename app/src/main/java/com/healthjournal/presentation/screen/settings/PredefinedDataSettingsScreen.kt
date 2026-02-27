package com.healthjournal.presentation.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.util.PredefinedData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredefinedDataSettingsScreen(
    onBack: () -> Unit,
    viewModel: PredefinedDataSettingsViewModel = viewModel()
) {
    val disabledSymptoms by viewModel.disabledSymptoms.collectAsStateWithLifecycle()
    val disabledMedications by viewModel.disabledMedications.collectAsStateWithLifecycle()
    val customSymptoms by viewModel.customSymptoms.collectAsStateWithLifecycle()
    val customMedications by viewModel.customMedications.collectAsStateWithLifecycle()

    var showAddSymptomDialog by remember { mutableStateOf(false) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var symptomsExpanded by rememberSaveable { mutableStateOf(false) }
    var medicationsExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.predefined_data_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Symptoms section header
            item(key = "symptoms_header") {
                SectionHeader(
                    icon = { Icon(Icons.Default.Healing, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                    title = stringResource(R.string.predefined_symptoms_section),
                    count = PredefinedData.symptoms.count { it.key !in disabledSymptoms } + customSymptoms.size,
                    total = PredefinedData.symptoms.size + customSymptoms.size,
                    expanded = symptomsExpanded,
                    onToggle = { symptomsExpanded = !symptomsExpanded },
                    onAdd = { showAddSymptomDialog = true }
                )
            }

            // Symptoms content (collapsible)
            if (symptomsExpanded) {
                items(PredefinedData.symptoms, key = { "s_${it.key}" }) { item ->
                    val enabled = item.key !in disabledSymptoms
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleSymptom(item.key, it) }
                    )
                }

                items(customSymptoms.toList(), key = { "cs_$it" }) { custom ->
                    CustomItem(
                        label = custom,
                        onRemove = { viewModel.removeCustomSymptom(custom) }
                    )
                }
            }

            item(key = "section_divider") { Spacer(modifier = Modifier.height(8.dp)) }

            // Medications section header
            item(key = "medications_header") {
                SectionHeader(
                    icon = { Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp)) },
                    title = stringResource(R.string.predefined_medications_section),
                    count = PredefinedData.medications.count { it.key !in disabledMedications } + customMedications.size,
                    total = PredefinedData.medications.size + customMedications.size,
                    expanded = medicationsExpanded,
                    onToggle = { medicationsExpanded = !medicationsExpanded },
                    onAdd = { showAddMedicationDialog = true }
                )
            }

            // Medications content (collapsible)
            if (medicationsExpanded) {
                items(PredefinedData.medications, key = { "m_${it.key}" }) { item ->
                    val enabled = item.key !in disabledMedications
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleMedication(item.key, it) }
                    )
                }

                items(customMedications.toList(), key = { "cm_$it" }) { custom ->
                    CustomItem(
                        label = custom,
                        onRemove = { viewModel.removeCustomMedication(custom) }
                    )
                }
            }
        }
    }

    if (showAddSymptomDialog) {
        AddCustomItemDialog(
            title = stringResource(R.string.add_custom_symptom),
            onDismiss = { showAddSymptomDialog = false },
            onConfirm = { name ->
                viewModel.addCustomSymptom(name)
                showAddSymptomDialog = false
            }
        )
    }

    if (showAddMedicationDialog) {
        AddCustomItemDialog(
            title = stringResource(R.string.add_custom_medication),
            onDismiss = { showAddMedicationDialog = false },
            onConfirm = { name ->
                viewModel.addCustomMedication(name)
                showAddMedicationDialog = false
            }
        )
    }
}

@Composable
private fun SectionHeader(
    icon: @Composable () -> Unit,
    title: String,
    count: Int,
    total: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$count / $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalIconButton(
                onClick = onAdd,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ToggleItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun CustomItem(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    stringResource(R.string.custom_label),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AddCustomItemDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
