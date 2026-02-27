package com.healthjournal.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.home_title)) })
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onAddVital,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(Icons.Default.MonitorHeart, contentDescription = stringResource(R.string.add_vital_desc))
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = onAddSymptom) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (recentVitals.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.recent_vitals),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(recentVitals, key = { it.id }) { vital ->
                    VitalCard(vital)
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            item {
                Text(
                    stringResource(R.string.symptoms_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (symptoms.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.no_symptoms_hint),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(symptoms, key = { it.id }) { symptom ->
                    SymptomCard(symptom, onDelete = { viewModel.removeSymptom(symptom) })
                }
            }
        }
    }
}

@Composable
private fun SymptomCard(symptom: Symptom, onDelete: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(symptom.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.intensity_format, symptom.intensity),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (symptom.triggers.isNotEmpty()) {
                    Text(
                        stringResource(R.string.triggers_format, symptom.triggers.joinToString(", ")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (symptom.notes.isNotBlank()) {
                    Text(
                        symptom.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    symptom.recordedAt.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
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

@Composable
private fun VitalCard(vital: VitalSign) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm") }
    val valueStr = if (vital.secondaryValue != null) {
        "${vital.value.toInt()}/${vital.secondaryValue.toInt()}"
    } else {
        vital.value.toString()
    }
    val displayName = vital.type.localizedDisplayName()
    val unit = vital.type.localizedUnit()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(displayName, style = MaterialTheme.typography.titleSmall)
                Text(
                    vital.recordedAt.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "$valueStr $unit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
