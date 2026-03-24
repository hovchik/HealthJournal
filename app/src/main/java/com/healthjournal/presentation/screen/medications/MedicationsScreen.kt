package com.healthjournal.presentation.screen.medications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onEditMedication: (Long) -> Unit = {},
    viewModel: MedicationsViewModel = viewModel()
) {
    val medications by viewModel.medications.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val diseases by viewModel.diseases.collectAsStateWithLifecycle()

    val activeProfileName = if (activeProfileId == 0L) {
        stringResource(R.string.rel_self)
    } else {
        familyMembers.find { it.id == activeProfileId }?.name ?: stringResource(R.string.rel_self)
    }

    val ungroupedLabel = stringResource(R.string.ungrouped)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.medications_title),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            stringResource(R.string.recording_for, activeProfileName),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedication,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_medication_desc)) }
            )
        }
    ) { padding ->
        if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier.padding(32.dp),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp),
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_medications_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            val medicationsByDisease = remember(medications, diseases) {
                val diseaseMap = diseases.associateBy { it.id }
                val grouped = medications.groupBy { it.diseaseId }
                grouped.entries.sortedWith(compareBy {
                    if (it.key == 0L) "\uFFFF" else diseaseMap[it.key]?.name ?: "\uFFFE"
                }).map { (diseaseId, meds) ->
                    val name = if (diseaseId == 0L) ungroupedLabel else diseaseMap[diseaseId]?.name ?: ungroupedLabel
                    DiseaseMedicationsGroup(diseaseId, name, meds)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                medicationsByDisease.forEach { group ->
                    item(key = "disease_header_${group.diseaseId}") {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (group.diseaseId != 0L)
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        ) {
                            Text(
                                group.diseaseName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (group.diseaseId != 0L) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    items(group.medications, key = { it.id }) { medication ->
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                            MedicationCard(
                                medication = medication,
                                onEdit = { onEditMedication(medication.id) },
                                onLogTaken = { viewModel.logTaken(medication) },
                                onToggleActive = { viewModel.toggleMedicationActive(medication) },
                                onDelete = { viewModel.removeMedication(medication) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class DiseaseMedicationsGroup(
    val diseaseId: Long,
    val diseaseName: String,
    val medications: List<Medication>
)

@Composable
private fun MedicationCard(
    medication: Medication,
    onEdit: () -> Unit,
    onLogTaken: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val accentColor = if (medication.active) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (medication.active) MaterialTheme.colorScheme.surfaceContainerLow
                else MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (medication.active) 2.dp else 0.dp
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(accentColor)
            )
            Column(modifier = Modifier.weight(1f).padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            medication.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                "${medication.dosage} — ${medication.frequency}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.since_date, medication.startDate.format(dateFormatter)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (!medication.active) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    stringResource(R.string.inactive),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.edit),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (medication.active) {
                            IconButton(onClick = onLogTaken, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = stringResource(R.string.taken_desc),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
