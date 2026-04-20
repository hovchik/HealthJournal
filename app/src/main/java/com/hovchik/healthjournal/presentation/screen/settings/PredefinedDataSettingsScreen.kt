package com.hovchik.healthjournal.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.util.PredefinedData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredefinedDataSettingsScreen(
    onBack: () -> Unit,
    viewModel: PredefinedDataSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val disabledSymptoms by viewModel.disabledSymptoms.collectAsStateWithLifecycle()
    val disabledMedications by viewModel.disabledMedications.collectAsStateWithLifecycle()
    val customSymptoms by viewModel.customSymptoms.collectAsStateWithLifecycle()
    val customMedications by viewModel.customMedications.collectAsStateWithLifecycle()
    val disabledRelations by viewModel.disabledRelations.collectAsStateWithLifecycle()
    val customRelations by viewModel.customRelations.collectAsStateWithLifecycle()
    val disabledGroups by viewModel.disabledGroups.collectAsStateWithLifecycle()
    val customGroups by viewModel.customGroups.collectAsStateWithLifecycle()

    var showAddSymptomDialog by remember { mutableStateOf(false) }
    var showAddMedicationDialog by remember { mutableStateOf(false) }
    var showAddRelationDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }
    var symptomsExpanded by rememberSaveable { mutableStateOf(false) }
    var medicationsExpanded by rememberSaveable { mutableStateOf(false) }
    var relationsExpanded by rememberSaveable { mutableStateOf(false) }
    var groupsExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
            // Search bar
            item(key = "search_bar") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true
                )
            }

            // Filter symptoms and medications by search query
            val filteredSymptoms = if (searchQuery.isBlank()) {
                PredefinedData.symptoms
            } else {
                PredefinedData.symptoms.filter { item ->
                    context.getString(item.nameResId).contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredCustomSymptoms = if (searchQuery.isBlank()) {
                customSymptoms.toList()
            } else {
                customSymptoms.filter { it.contains(searchQuery, ignoreCase = true) }.toList()
            }
            val filteredMedications = if (searchQuery.isBlank()) {
                PredefinedData.medications
            } else {
                PredefinedData.medications.filter { item ->
                    context.getString(item.nameResId).contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredCustomMedications = if (searchQuery.isBlank()) {
                customMedications.toList()
            } else {
                customMedications.filter { it.contains(searchQuery, ignoreCase = true) }.toList()
            }

            val filteredRelations = if (searchQuery.isBlank()) {
                PredefinedData.relationshipItems
            } else {
                PredefinedData.relationshipItems.filter { item ->
                    context.getString(item.nameResId).contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredCustomRelations = if (searchQuery.isBlank()) {
                customRelations.toList()
            } else {
                customRelations.filter { it.contains(searchQuery, ignoreCase = true) }.toList()
            }

            val filteredGroups = if (searchQuery.isBlank()) {
                PredefinedData.groupItems
            } else {
                PredefinedData.groupItems.filter { item ->
                    context.getString(item.nameResId).contains(searchQuery, ignoreCase = true)
                }
            }
            val filteredCustomGroups = if (searchQuery.isBlank()) {
                customGroups.toList()
            } else {
                customGroups.filter { it.contains(searchQuery, ignoreCase = true) }.toList()
            }

            val isSearching = searchQuery.isNotBlank()

            // Symptoms section header
            item(key = "symptoms_header") {
                SectionHeader(
                    icon = { Icon(Icons.Default.Healing, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                    title = stringResource(R.string.predefined_symptoms_section),
                    count = PredefinedData.symptoms.count { it.key !in disabledSymptoms } + customSymptoms.size,
                    total = PredefinedData.symptoms.size + customSymptoms.size,
                    expanded = symptomsExpanded || isSearching,
                    onToggle = { if (!isSearching) symptomsExpanded = !symptomsExpanded },
                    onAdd = { showAddSymptomDialog = true }
                )
            }

            // Symptoms content (collapsible, or always show when searching)
            if (symptomsExpanded || isSearching) {
                items(filteredSymptoms, key = { "s_${it.key}" }) { item ->
                    val enabled = item.key !in disabledSymptoms
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleSymptom(item.key, it) }
                    )
                }

                items(filteredCustomSymptoms, key = { "cs_$it" }) { custom ->
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
                    expanded = medicationsExpanded || isSearching,
                    onToggle = { if (!isSearching) medicationsExpanded = !medicationsExpanded },
                    onAdd = { showAddMedicationDialog = true }
                )
            }

            // Medications content (collapsible, or always show when searching)
            if (medicationsExpanded || isSearching) {
                items(filteredMedications, key = { "m_${it.key}" }) { item ->
                    val enabled = item.key !in disabledMedications
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleMedication(item.key, it) }
                    )
                }

                items(filteredCustomMedications, key = { "cm_$it" }) { custom ->
                    CustomItem(
                        label = custom,
                        onRemove = { viewModel.removeCustomMedication(custom) }
                    )
                }
            }

            item(key = "section_divider_2") { Spacer(modifier = Modifier.height(8.dp)) }

            // Relations section header
            item(key = "relations_header") {
                SectionHeader(
                    icon = { Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(22.dp)) },
                    title = stringResource(R.string.predefined_relations_section),
                    count = PredefinedData.relationshipItems.count { it.key !in disabledRelations } + customRelations.size,
                    total = PredefinedData.relationshipItems.size + customRelations.size,
                    expanded = relationsExpanded || isSearching,
                    onToggle = { if (!isSearching) relationsExpanded = !relationsExpanded },
                    onAdd = { showAddRelationDialog = true }
                )
            }

            // Relations content
            if (relationsExpanded || isSearching) {
                items(filteredRelations, key = { "r_${it.key}" }) { item ->
                    val enabled = item.key !in disabledRelations
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleRelation(item.key, it) }
                    )
                }

                items(filteredCustomRelations, key = { "cr_$it" }) { custom ->
                    CustomItem(
                        label = custom,
                        onRemove = { viewModel.removeCustomRelation(custom) }
                    )
                }
            }

            item(key = "section_divider_3") { Spacer(modifier = Modifier.height(8.dp)) }

            // Groups section header
            item(key = "groups_header") {
                SectionHeader(
                    icon = { Icon(Icons.Default.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp)) },
                    title = stringResource(R.string.predefined_groups_section),
                    count = PredefinedData.groupItems.count { it.key !in disabledGroups } + customGroups.size,
                    total = PredefinedData.groupItems.size + customGroups.size,
                    expanded = groupsExpanded || isSearching,
                    onToggle = { if (!isSearching) groupsExpanded = !groupsExpanded },
                    onAdd = { showAddGroupDialog = true }
                )
            }

            // Groups content
            if (groupsExpanded || isSearching) {
                items(filteredGroups, key = { "g_${it.key}" }) { item ->
                    val enabled = item.key !in disabledGroups
                    val label = stringResource(item.nameResId)
                    ToggleItem(
                        label = label,
                        checked = enabled,
                        onCheckedChange = { viewModel.toggleGroup(item.key, it) }
                    )
                }

                items(filteredCustomGroups, key = { "cg_$it" }) { custom ->
                    CustomItem(
                        label = custom,
                        onRemove = { viewModel.removeCustomGroup(custom) }
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

    if (showAddRelationDialog) {
        AddCustomItemDialog(
            title = stringResource(R.string.add_custom_relation),
            onDismiss = { showAddRelationDialog = false },
            onConfirm = { name ->
                viewModel.addCustomRelation(name)
                showAddRelationDialog = false
            }
        )
    }

    if (showAddGroupDialog) {
        AddCustomItemDialog(
            title = stringResource(R.string.add_custom_group),
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { name ->
                viewModel.addCustomGroup(name)
                showAddGroupDialog = false
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
        icon = {
            Icon(Icons.Default.Add, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        },
        title = {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            )
        },
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text.trim()) },
                enabled = text.isNotBlank(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = MaterialTheme.shapes.medium) {
                Text(stringResource(R.string.close))
            }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}
