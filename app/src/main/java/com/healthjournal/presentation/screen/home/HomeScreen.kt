package com.healthjournal.presentation.screen.home

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
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSymptom: () -> Unit,
    onAddVital: () -> Unit,
    onEditSymptom: (Long) -> Unit = {},
    onEditVital: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val symptoms by viewModel.symptoms.collectAsStateWithLifecycle()
    val recentVitals by viewModel.recentVitals.collectAsStateWithLifecycle()
    val activeProfileId by viewModel.activeProfileId.collectAsStateWithLifecycle()
    val familyMembers by viewModel.familyMembers.collectAsStateWithLifecycle()

    val activeProfileName = if (activeProfileId == 0L) {
        stringResource(R.string.rel_self)
    } else {
        familyMembers.find { it.id == activeProfileId }?.name ?: stringResource(R.string.rel_self)
    }

    val todayLabel = stringResource(R.string.date_today)
    val yesterdayLabel = stringResource(R.string.date_yesterday)
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.home_title),
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
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = onAddVital,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.add_vital_desc))
                }
                ExtendedFloatingActionButton(
                    onClick = onAddSymptom,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.add_symptom_desc)) }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (recentVitals.isNotEmpty()) {
                item(key = "vitals_header") {
                    SectionHeader(
                        icon = Icons.Default.MonitorHeart,
                        title = stringResource(R.string.recent_vitals),
                        color = MaterialTheme.colorScheme.tertiary,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

                val vitalsByDate = recentVitals.groupBy { it.recordedAt.toLocalDate() }
                    .toSortedMap(compareByDescending { it })
                vitalsByDate.forEach { (date, vitalsForDate) ->
                    item(key = "vital_date_$date") {
                        DateHeader(date, todayLabel, yesterdayLabel, dateFormatter)
                    }
                    items(vitalsForDate, key = { "vital_${it.id}" }) { vital ->
                        AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                            VitalCard(vital, onEdit = { onEditVital(vital.id) })
                        }
                    }
                }
                item(key = "vitals_spacer") { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item(key = "symptoms_header") {
                SectionHeader(
                    icon = Icons.Default.Sick,
                    title = stringResource(R.string.symptoms_title),
                    color = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            }

            if (symptoms.isEmpty()) {
                item(key = "empty_symptoms") {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = MaterialTheme.shapes.large,
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(64.dp),
                                tonalElevation = 2.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.EditNote, contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.no_symptoms_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                val symptomsByDate = symptoms.groupBy { it.recordedAt.toLocalDate() }
                    .toSortedMap(compareByDescending { it })
                symptomsByDate.forEach { (date, symptomsForDate) ->
                    item(key = "symptom_date_$date") {
                        DateHeader(date, todayLabel, yesterdayLabel, dateFormatter)
                    }
                    items(symptomsForDate, key = { "symptom_${it.id}" }) { symptom ->
                        SymptomCard(
                            symptom,
                            onEdit = { onEditSymptom(symptom.id) },
                            onDelete = { viewModel.removeSymptom(symptom) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor,
            modifier = Modifier.size(36.dp),
            tonalElevation = 1.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null,
                    tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
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
        label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun SymptomCard(symptom: Symptom, onEdit: () -> Unit, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val intensityColor = when {
        symptom.intensity <= 3 -> MaterialTheme.colorScheme.primary
        symptom.intensity <= 6 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

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
                    .background(intensityColor)
            )
            Row(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.small,
                    color = intensityColor.copy(alpha = 0.12f),
                    tonalElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${symptom.intensity}",
                            style = MaterialTheme.typography.titleMedium,
                            color = intensityColor, fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(symptom.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (!symptom.value.isNullOrBlank()) {
                        Text(
                            symptom.value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.intensity_format, symptom.intensity),
                        style = MaterialTheme.typography.bodySmall, color = intensityColor
                    )
                    if (symptom.triggers.isNotEmpty()) {
                        Text(
                            stringResource(R.string.triggers_format, symptom.triggers.joinToString(", ")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (symptom.notes.isNotBlank()) {
                        Text(symptom.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    if (symptom.attachmentPaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AttachFile, contentDescription = null,
                                modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                            Text("${symptom.attachmentPaths.size}", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(symptom.recordedAt.format(formatter), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
                Column {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalCard(vital: VitalSign, onEdit: () -> Unit = {}) {
    val formatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val valueStr = if (vital.secondaryValue != null) {
        "${vital.value.toInt()}/${vital.secondaryValue.toInt()}"
    } else vital.value.toString()
    val displayName = vital.type.localizedDisplayName()
    val unit = vital.type.localizedUnit()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column {
                    Text(displayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(vital.recordedAt.format(formatter), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ) {
                Text(
                    "$valueStr $unit",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
