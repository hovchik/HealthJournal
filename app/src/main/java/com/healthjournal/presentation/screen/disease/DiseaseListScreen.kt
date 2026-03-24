package com.healthjournal.presentation.screen.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.RestoreFromTrash
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.Disease
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseListScreen(
    onDiseaseClick: (Long) -> Unit,
    onAddSymptom: () -> Unit = {},
    onAddVital: () -> Unit = {},
    viewModel: DiseaseViewModel = viewModel()
) {
    val diseases by viewModel.diseases.collectAsStateWithLifecycle()
    val deletedDiseases by viewModel.deletedDiseases.collectAsStateWithLifecycle()
    val allSymptoms by viewModel.allSymptoms.collectAsStateWithLifecycle()
    val allVitals by viewModel.allVitals.collectAsStateWithLifecycle()
    val allMedications by viewModel.allMedications.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeletedSection by remember { mutableStateOf(false) }
    var confirmDeleteDisease by remember { mutableStateOf<Disease?>(null) }
    var selectedGroup by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val undoLabel = stringResource(R.string.undo)
    val deletedLabel = stringResource(R.string.disease_deleted)

    val activeProfileName = if (activeProfileId == 0L) {
        stringResource(R.string.rel_self)
    } else {
        familyMembers.find { it.id == activeProfileId }?.name ?: stringResource(R.string.rel_self)
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    // Group diseases by their group field
    val groupedDiseases = remember(diseases) {
        diseases.groupBy { it.group.ifBlank { "" } }
            .toSortedMap(compareBy { if (it.isEmpty()) "\uFFFF" else it })
    }

    val allGroups = remember(groupedDiseases) {
        groupedDiseases.keys.toList()
    }

    val filteredDiseases = remember(diseases, selectedGroup) {
        if (selectedGroup == null) diseases
        else diseases.filter { (it.group.ifBlank { "" }) == selectedGroup }
    }

    val activeCount = diseases.count { it.active }
    val resolvedCount = diseases.count { !it.active }

    // Show snackbar when disease is deleted
    LaunchedEffect(recentlyDeleted) {
        recentlyDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = deletedLabel,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearRecentlyDeleted()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.home_title),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.recording_for, activeProfileName),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (deletedDiseases.isNotEmpty()) {
                        IconButton(onClick = { showDeletedSection = !showDeletedSection }) {
                            Icon(
                                if (showDeletedSection) Icons.Default.DeleteSweep else Icons.Outlined.RestoreFromTrash,
                                contentDescription = if (showDeletedSection) stringResource(R.string.hide_deleted) else stringResource(R.string.show_deleted),
                                tint = if (showDeletedSection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_disease))
            }
        }
    ) { padding ->
        if (diseases.isEmpty() && !showDeletedSection) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(88.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.LocalHospital,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.no_diseases_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    FilledTonalButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_disease))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick stats row
                if (diseases.isNotEmpty()) {
                    item(key = "stats") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatCard(
                                label = stringResource(R.string.symptoms_title),
                                count = allSymptoms.size,
                                icon = Icons.Default.Sick,
                                color = MaterialTheme.colorScheme.primary,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.nav_vitals),
                                count = allVitals.size,
                                icon = Icons.Default.MonitorHeart,
                                color = MaterialTheme.colorScheme.tertiary,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                label = stringResource(R.string.nav_medications),
                                count = allMedications.size,
                                icon = Icons.Default.Medication,
                                color = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Group filter chips
                if (allGroups.size > 1 || (allGroups.size == 1 && allGroups.first().isNotEmpty())) {
                    item(key = "group_filters") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedGroup == null,
                                    onClick = { selectedGroup = null },
                                    label = { Text(stringResource(R.string.all)) },
                                    leadingIcon = if (selectedGroup == null) {
                                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                            items(allGroups, key = { "filter_$it" }) { group ->
                                val label = group.ifBlank { stringResource(R.string.ungrouped) }
                                FilterChip(
                                    selected = selectedGroup == group,
                                    onClick = { selectedGroup = if (selectedGroup == group) null else group },
                                    label = { Text(label) },
                                    leadingIcon = if (selectedGroup == group) {
                                        { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                    } else {
                                        { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    }
                                )
                            }
                        }
                    }
                }

                // Deleted diseases section
                if (showDeletedSection && deletedDiseases.isNotEmpty()) {
                    item(key = "deleted_header") {
                        DeletedDiseasesSectionHeader()
                    }
                    items(deletedDiseases, key = { "deleted_${it.id}" }) { disease ->
                        DeletedDiseaseCard(
                            disease = disease,
                            dateFormatter = dateFormatter,
                            onRestore = { viewModel.restoreDisease(disease) },
                            onPermanentDelete = { confirmDeleteDisease = disease }
                        )
                    }
                    item(key = "deleted_divider") {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                // Active diseases - grouped or filtered
                if (selectedGroup != null) {
                    items(filteredDiseases, key = { it.id }) { disease ->
                        val symptomCount = allSymptoms.count { it.diseaseId == disease.id }
                        val vitalCount = allVitals.count { it.diseaseId == disease.id }
                        val medCount = allMedications.count { it.diseaseId == disease.id }

                        DiseaseCard(
                            disease = disease,
                            symptomCount = symptomCount,
                            vitalCount = vitalCount,
                            medicationCount = medCount,
                            dateFormatter = dateFormatter,
                            onClick = { onDiseaseClick(disease.id) },
                            onDelete = { viewModel.deleteDisease(disease) }
                        )
                    }
                } else {
                    groupedDiseases.forEach { (group, diseasesInGroup) ->
                        if (groupedDiseases.size > 1 || group.isNotEmpty()) {
                            item(key = "group_header_$group") {
                                GroupHeader(
                                    group = group.ifBlank { stringResource(R.string.ungrouped) },
                                    count = diseasesInGroup.size
                                )
                            }
                        }

                        items(diseasesInGroup, key = { it.id }) { disease ->
                            val symptomCount = allSymptoms.count { it.diseaseId == disease.id }
                            val vitalCount = allVitals.count { it.diseaseId == disease.id }
                            val medCount = allMedications.count { it.diseaseId == disease.id }

                            DiseaseCard(
                                disease = disease,
                                symptomCount = symptomCount,
                                vitalCount = vitalCount,
                                medicationCount = medCount,
                                dateFormatter = dateFormatter,
                                onClick = { onDiseaseClick(disease.id) },
                                onDelete = { viewModel.deleteDisease(disease) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDiseaseDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, notes, group ->
                viewModel.addDisease(name, notes, group)
                showAddDialog = false
            }
        )
    }

    // Confirm permanent delete dialog
    confirmDeleteDisease?.let { disease ->
        AlertDialog(
            onDismissRequest = { confirmDeleteDisease = null },
            title = { Text(stringResource(R.string.permanently_delete)) },
            text = { Text(stringResource(R.string.confirm_permanent_delete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.permanentlyDeleteDisease(disease)
                        confirmDeleteDisease = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteDisease = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor.copy(alpha = 0.4f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(
                "$count",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun GroupHeader(group: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            group,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun DeletedDiseasesSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Text(
            stringResource(R.string.deleted_diseases),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun DeletedDiseaseCard(
    disease: Disease,
    dateFormatter: DateTimeFormatter,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    disease.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.deleted_date, disease.deletedAt?.format(dateFormatter) ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onRestore) {
                Icon(
                    Icons.Outlined.RestoreFromTrash,
                    contentDescription = stringResource(R.string.restore),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onPermanentDelete) {
                Icon(
                    Icons.Outlined.DeleteOutline,
                    contentDescription = stringResource(R.string.permanently_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DiseaseCard(
    disease: Disease,
    symptomCount: Int,
    vitalCount: Int,
    medicationCount: Int,
    dateFormatter: DateTimeFormatter,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = if (disease.active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Top colored accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = accentColor.copy(alpha = 0.12f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                disease.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    disease.createdAt.format(dateFormatter),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                if (!disease.active) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            stringResource(R.string.disease_resolved),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (disease.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        disease.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Group badge
                if (disease.group.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                disease.group,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Count chips in a Surface row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CountChip(
                            icon = Icons.Default.Sick,
                            count = symptomCount,
                            label = stringResource(R.string.symptoms_title),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                        CountChip(
                            icon = Icons.Default.MonitorHeart,
                            count = vitalCount,
                            label = stringResource(R.string.nav_vitals),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        )
                        CountChip(
                            icon = Icons.Default.Medication,
                            count = medicationCount,
                            label = stringResource(R.string.nav_medications),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = color)
            Text("$count", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AddDiseaseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, notes: String, group: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_disease)) },
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
                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text(stringResource(R.string.disease_group)) },
                    placeholder = { Text(stringResource(R.string.disease_group_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, notes, group) },
                enabled = name.isNotBlank()
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
