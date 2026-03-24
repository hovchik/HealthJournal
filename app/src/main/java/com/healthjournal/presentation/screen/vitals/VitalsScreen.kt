package com.healthjournal.presentation.screen.vitals

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
import androidx.compose.material.icons.filled.MonitorHeart
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
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.presentation.components.VitalsChart
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsScreen(
    onAddVital: () -> Unit = {},
    onEditVital: (Long) -> Unit = {},
    viewModel: VitalsViewModel = viewModel()
) {
    val vitals by viewModel.vitals.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()
    val diseases by viewModel.diseases.collectAsStateWithLifecycle()

    val activeProfileName = if (activeProfileId == 0L) {
        stringResource(R.string.rel_self)
    } else {
        familyMembers.find { it.id == activeProfileId }?.name ?: stringResource(R.string.rel_self)
    }

    val todayLabel = stringResource(R.string.date_today)
    val yesterdayLabel = stringResource(R.string.date_yesterday)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val ungroupedLabel = stringResource(R.string.ungrouped)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.vitals_title),
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
                onClick = onAddVital,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_vital_desc)) }
            )
        }
    ) { padding ->
        if (vitals.isEmpty()) {
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
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(72.dp),
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_vitals_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            val vitalsByDisease = remember(vitals, diseases) {
                val diseaseMap = diseases.associateBy { it.id }
                val grouped = vitals.groupBy { it.diseaseId }
                grouped.entries.sortedWith(compareBy {
                    if (it.key == 0L) "\uFFFF" else diseaseMap[it.key]?.name ?: "\uFFFE"
                }).map { (diseaseId, vitalsList) ->
                    val name = if (diseaseId == 0L) ungroupedLabel else diseaseMap[diseaseId]?.name ?: ungroupedLabel
                    DiseaseVitalsGroup(diseaseId, name, vitalsList)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                vitalsByDisease.forEach { group ->
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

                    val vitalsByType = group.vitals.groupBy { it.type }
                    val chartTypes = vitalsByType.filter { it.value.size >= 2 }
                    if (chartTypes.isNotEmpty()) {
                        item(key = "charts_header_${group.diseaseId}") {
                            Text(
                                stringResource(R.string.vitals_charts_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        chartTypes.forEach { (type, typeVitals) ->
                            item(key = "chart_${group.diseaseId}_${type.name}") {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    VitalsChart(
                                        title = type.localizedDisplayName(),
                                        vitals = typeVitals.sortedBy { it.recordedAt },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    val vitalsByDate = group.vitals
                        .groupBy { it.recordedAt.toLocalDate() }
                        .toSortedMap(compareByDescending { it })

                    vitalsByDate.forEach { (date, vitalsForDate) ->
                        item(key = "date_${group.diseaseId}_$date") {
                            DateHeader(date, todayLabel, yesterdayLabel, dateFormatter)
                        }
                        items(vitalsForDate, key = { it.id }) { vital ->
                            AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                                VitalDetailCard(
                                    vital,
                                    onEdit = { onEditVital(vital.id) },
                                    onDelete = { viewModel.removeVital(vital) }
                                )
                            }
                        }
                    }

                    item(key = "divider_${group.diseaseId}") {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

private data class DiseaseVitalsGroup(
    val diseaseId: Long,
    val diseaseName: String,
    val vitals: List<VitalSign>
)

@Composable
private fun DateHeader(
    date: LocalDate,
    todayLabel: String,
    yesterdayLabel: String,
    dateFormatter: DateTimeFormatter
) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> todayLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(dateFormatter)
    }
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun VitalDetailCard(vital: VitalSign, onEdit: () -> Unit, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val valueStr = if (vital.secondaryValue != null) {
        "${vital.value.toInt()}/${vital.secondaryValue.toInt()}"
    } else {
        vital.value.toString()
    }
    val displayName = vital.type.localizedDisplayName()
    val unit = vital.type.localizedUnit()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text(
                            "$valueStr $unit",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (vital.notes.isNotBlank()) {
                        Text(
                            vital.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        vital.recordedAt.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
