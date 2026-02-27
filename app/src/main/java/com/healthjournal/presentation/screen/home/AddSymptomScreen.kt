package com.healthjournal.presentation.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.util.AttachmentHelper
import com.healthjournal.util.PredefinedData
import com.healthjournal.util.PredefinedDataKeys
import com.healthjournal.util.predefinedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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

    val disabledSymptoms by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())
    val customSymptoms by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())

    val enabledPredefined = PredefinedData.symptoms.filter { it.key !in disabledSymptoms }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.symptom_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                stringResource(R.string.common_symptoms),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                enabledPredefined.forEach { item ->
                    val label = stringResource(item.nameResId)
                    SuggestionChip(
                        onClick = { name = label },
                        label = { Text(label) }
                    )
                }
                customSymptoms.forEach { custom ->
                    SuggestionChip(
                        onClick = { name = custom },
                        label = { Text(custom) }
                    )
                }
            }

            Column {
                Text(stringResource(R.string.intensity_label, intensity.toInt()))
                Slider(
                    value = intensity,
                    onValueChange = { intensity = it },
                    valueRange = 0f..10f,
                    steps = 9
                )
            }

            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text(stringResource(R.string.duration_minutes)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = triggers,
                onValueChange = { triggers = it },
                label = { Text(stringResource(R.string.triggers_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.attach_file))
            }

            if (attachmentPaths.isNotEmpty()) {
                Text(
                    stringResource(R.string.attachments_count, attachmentPaths.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addNewSymptom(
                            name = name,
                            intensity = intensity.toInt(),
                            durationMinutes = duration.toIntOrNull(),
                            triggers = triggers.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            notes = notes,
                            attachmentPaths = attachmentPaths
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
