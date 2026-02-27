package com.healthjournal.presentation.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.domain.model.FamilyMember
import com.healthjournal.R
import com.healthjournal.util.AttachmentHelper
import com.healthjournal.util.PredefinedData
import com.healthjournal.util.PredefinedDataKeys
import com.healthjournal.util.predefinedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

private data class ProfileUiModel(val id: Long, val label: String)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddSymptomScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(5f) }
    var duration by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var attachmentPaths by remember { mutableStateOf(listOf<String>()) }
    var selectedCommonSymptoms by remember { mutableStateOf(setOf<String>()) }

    val familyMembers: List<FamilyMember> by viewModel.familyMembers.collectAsState(initial = emptyList())
    val activeProfileId: Long by viewModel.activeProfileId.collectAsState(initial = 0L)
    val selfLabel = stringResource(R.string.rel_self)
    val profiles = remember(familyMembers, selfLabel) {
        buildList {
            add(ProfileUiModel(0L, selfLabel))
            familyMembers.forEach { add(ProfileUiModel(it.id, it.name)) }
        }
    }

    var selectedProfileId by remember(activeProfileId) { mutableStateOf(activeProfileId) }
    var profileExpanded by remember { mutableStateOf(false) }

    val disabledSymptoms by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet<String>())

    val customSymptoms by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet<String>())

    val enabledPredefined = remember(disabledSymptoms) {
        PredefinedData.symptoms.filter { it.key !in disabledSymptoms }
    }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val paths = uris.mapNotNull { uri -> AttachmentHelper.copyToInternal(context, uri) }
        attachmentPaths = attachmentPaths + paths
    }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_symptom_title)) },
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
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.add_symptom_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Column {
                    OutlinedTextField(
                        value = profiles.firstOrNull { it.id == selectedProfileId }?.label ?: selfLabel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.select_person)) },
                        modifier = Modifier
                            .clickable { profileExpanded = true }
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = profileExpanded,
                        onDismissRequest = { profileExpanded = false }
                    ) {
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.label) },
                                onClick = {
                                    selectedProfileId = profile.id
                                    profileExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.symptom_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                Text(
                    stringResource(R.string.common_symptoms),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    enabledPredefined.forEach { item ->
                        val label = stringResource(item.nameResId)
                        val selected = selectedCommonSymptoms.contains(label)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedCommonSymptoms = if (selected) {
                                    selectedCommonSymptoms - label
                                } else {
                                    selectedCommonSymptoms + label
                                }
                                name = selectedCommonSymptoms.joinToString(", ")
                            },
                            label = { Text(label) }
                        )
                    }
                    customSymptoms.forEach { custom ->
                        val selected = selectedCommonSymptoms.contains(custom)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedCommonSymptoms = if (selected) {
                                    selectedCommonSymptoms - custom
                                } else {
                                    selectedCommonSymptoms + custom
                                }
                                name = selectedCommonSymptoms.joinToString(", ")
                            },
                            label = { Text(custom) }
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.intensity_label, intensity.toInt()))
                        Slider(
                            value = intensity,
                            onValueChange = { intensity = it },
                            valueRange = 0f..10f,
                            steps = 9
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text(stringResource(R.string.duration_minutes)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = triggers,
                        onValueChange = { triggers = it },
                        label = { Text(stringResource(R.string.triggers_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
            }

            item {
                OutlinedButton(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.attach_file))
                }
            }

            if (attachmentPaths.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.attachments_count, attachmentPaths.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(attachmentPaths) { path ->
                    AssistChip(
                        onClick = {},
                        label = { Text(path.substringAfterLast('/')) },
                        trailingIcon = {
                            IconButton(onClick = { attachmentPaths = attachmentPaths - path }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete))
                            }
                        }
                    )
                }
            }

            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addNewSymptom(
                                name = name,
                                intensity = intensity.toInt(),
                                durationMinutes = duration.toIntOrNull(),
                                triggers = triggers.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                notes = notes,
                                profileId = selectedProfileId,
                                attachmentPaths = attachmentPaths
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    enabled = name.isNotBlank(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
