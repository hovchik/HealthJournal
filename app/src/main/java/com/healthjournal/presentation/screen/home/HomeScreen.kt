package com.healthjournal.presentation.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSymptom: () -> Unit,
    onAddVital: () -> Unit,
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
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.add_vital_desc))
                }
                FloatingActionButton(
                    onClick = onAddSymptom,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_symptom_desc))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (recentVitals.isNotEmpty()) {
                item(key = "vitals_header") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.MonitorHeart, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.recent_vitals),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                items(recentVitals, key = { "vital_${it.id}" }) { vital ->
                    AnimatedVisibility(visible = true, enter = fadeIn() + slideInVertically()) {
                        VitalCard(vital)
                    }
                }
                item(key = "vitals_spacer") { Spacer(modifier = Modifier.height(4.dp)) }
            }

            item(key = "symptoms_header") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Sick, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.symptoms_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (symptoms.isEmpty()) {
                item(key = "empty_symptoms") {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.EditNote, contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.no_symptoms_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(symptoms, key = { "symptom_${it.id}" }) { symptom ->
                    SymptomCard(symptom, onDelete = { viewModel.removeSymptom(symptom) })
                }
            }
        }
    }
}

@Composable
private fun SymptomCard(symptom: Symptom, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val intensityColor = when {
        symptom.intensity <= 3 -> MaterialTheme.colorScheme.primary
        symptom.intensity <= 6 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = MaterialTheme.shapes.medium,
                color = intensityColor.copy(alpha = 0.12f)
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    stringResource(R.string.intensity_format, symptom.intensity),
                    style = MaterialTheme.typography.bodySmall, color = intensityColor
                )
                if (symptom.triggers.isNotEmpty()) {
                    Text(
                        stringResource(R.string.triggers_format, symptom.triggers.joinToString(", ")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (symptom.notes.isNotBlank()) {
                    Text(symptom.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun VitalCard(vital: VitalSign) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val valueStr = if (vital.secondaryValue != null) {
        "${vital.value.toInt()}/${vital.secondaryValue.toInt()}"
    } else vital.value.toString()
    val displayName = vital.type.localizedDisplayName()
    val unit = vital.type.localizedUnit()

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(displayName, style = MaterialTheme.typography.titleSmall)
                    Text(vital.recordedAt.format(formatter), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("$valueStr $unit", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
        }
    }
}
