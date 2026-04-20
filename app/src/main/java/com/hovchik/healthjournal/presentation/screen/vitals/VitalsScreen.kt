package com.hovchik.healthjournal.presentation.screen.vitals

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hovchik.healthjournal.R
import com.hovchik.healthjournal.domain.model.VitalSign
import com.hovchik.healthjournal.domain.model.VitalType
import com.hovchik.healthjournal.presentation.components.VitalsChart
import com.hovchik.healthjournal.util.localizedDisplayName
import com.hovchik.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ── Vital type visual mapping ──────────────────────────────────────────────────

private fun VitalType.icon(): ImageVector = when (this) {
    VitalType.BLOOD_PRESSURE -> Icons.Default.Favorite
    VitalType.PULSE          -> Icons.Default.MonitorHeart
    VitalType.TEMPERATURE    -> Icons.Default.Thermostat
    VitalType.SPO2           -> Icons.Default.Air
    VitalType.GLUCOSE        -> Icons.Default.Water
    VitalType.WEIGHT         -> Icons.Default.FitnessCenter
    VitalType.SLEEP          -> Icons.Default.Bedtime
    VitalType.STEPS          -> Icons.Default.DirectionsWalk
}

private fun VitalType.tintColor(): Color = when (this) {
    VitalType.BLOOD_PRESSURE -> Color(0xFFE53935)  // Red — BP
    VitalType.PULSE          -> Color(0xFFE91E63)  // Pink — Heart rate
    VitalType.TEMPERATURE    -> Color(0xFFF57C00)  // Orange — Temp
    VitalType.SPO2           -> Color(0xFF1565C0)  // Blue — Oxygen
    VitalType.GLUCOSE        -> Color(0xFF6A1B9A)  // Purple — Glucose
    VitalType.WEIGHT         -> Color(0xFF00796B)  // Teal — Weight
    VitalType.SLEEP          -> Color(0xFF283593)  // Indigo — Sleep
    VitalType.STEPS          -> Color(0xFF2E7D32)  // Green — Steps
}

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
                shape = MaterialTheme.shapes.extraLarge,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add_vital_desc), fontWeight = FontWeight.SemiBold) }
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
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(40.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MonitorHeart,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.no_vitals_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
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
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                vitalsByDisease.forEach { group ->
                    item(key = "disease_header_${group.diseaseId}") {
                        DiseaseSectionHeader(group, padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp))
                    }

                    val vitalsByType = group.vitals.groupBy { it.type }
                    val chartTypes = vitalsByType.filter { it.value.size >= 2 }
                    if (chartTypes.isNotEmpty()) {
                        item(key = "charts_header_${group.diseaseId}") {
                            Text(
                                stringResource(R.string.vitals_charts_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        chartTypes.forEach { (type, typeVitals) ->
                            item(key = "chart_${group.diseaseId}_${type.name}") {
                                VitalChartCard(
                                    type = type,
                                    typeVitals = typeVitals,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
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
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(280)) + slideInVertically(tween(280)) { it / 3 }
                            ) {
                                VitalDetailCard(
                                    vital,
                                    onEdit = { onEditVital(vital.id) },
                                    onDelete = { viewModel.removeVital(vital) }
                                )
                            }
                        }
                    }

                    item(key = "divider_${group.diseaseId}") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
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
private fun DiseaseSectionHeader(group: DiseaseVitalsGroup, padding: PaddingValues) {
    if (group.diseaseId != 0L) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = group.diseaseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VitalChartCard(type: VitalType, typeVitals: List<VitalSign>, modifier: Modifier = Modifier) {
    val typeColor = type.tintColor()
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = typeColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(type.icon(), contentDescription = null, tint = typeColor, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    text = type.localizedDisplayName(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor
                )
            }
            VitalsChart(
                title = "",
                vitals = typeVitals.sortedBy { it.recordedAt },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

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
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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
    val typeColor = vital.type.tintColor()
    val typeIcon = vital.type.icon()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Type-colored accent bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(typeColor, typeColor.copy(alpha = 0.4f))),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type icon
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = typeColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = null,
                            tint = typeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Value chip
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = typeColor.copy(alpha = 0.10f),
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = "$valueStr $unit",
                            style = MaterialTheme.typography.headlineSmall,
                            color = typeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    if (vital.notes.isNotBlank()) {
                        Text(
                            text = vital.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = vital.recordedAt.format(formatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = typeColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
