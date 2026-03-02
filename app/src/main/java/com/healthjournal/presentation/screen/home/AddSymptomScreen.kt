package com.healthjournal.presentation.screen.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSymptomScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(5f) }
    var value by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var attachmentPaths by remember { mutableStateOf(listOf<String>()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val disabledSymptoms by remember {
        context.predefinedDataStore.data.map { prefs -> prefs[PredefinedDataKeys.DISABLED_SYMPTOMS] ?: emptySet() }
    }.collectAsState(initial = emptySet())
    val customSymptoms by remember {
        context.predefinedDataStore.data.map { prefs -> prefs[PredefinedDataKeys.CUSTOM_SYMPTOMS] ?: emptySet() }
    }.collectAsState(initial = emptySet())

    val allSymptomLabels = remember(disabledSymptoms, customSymptoms) {
        val predefined = PredefinedData.symptoms.filter { it.key !in disabledSymptoms }
        predefined.map { it.key to it.nameResId } to customSymptoms.toList()
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        val paths = uris.mapNotNull { uri -> AttachmentHelper.copyToInternal(context, uri) }
        attachmentPaths = attachmentPaths + paths
    }

    LaunchedEffect(Unit) { viewModel.saveSuccess.collectLatest { onBack() } }

    val intensityColor = when {
        intensity.toInt() <= 3 -> MaterialTheme.colorScheme.primary
        intensity.toInt() <= 6 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Symptom name dropdown with search
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; dropdownExpanded = true },
                    label = { Text(stringResource(R.string.symptom_name)) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
                )
                val filteredPredefined = allSymptomLabels.first.filter { (_, resId) ->
                    name.isBlank() || context.getString(resId).contains(name, ignoreCase = true)
                }
                val filteredCustom = allSymptomLabels.second.filter {
                    name.isBlank() || it.contains(name, ignoreCase = true)
                }
                if (filteredPredefined.isNotEmpty() || filteredCustom.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        filteredPredefined.forEach { (_, resId) ->
                            val label = stringResource(resId)
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = { name = label; dropdownExpanded = false }
                            )
                        }
                        filteredCustom.forEach { custom ->
                            DropdownMenuItem(
                                text = { Text(custom) },
                                onClick = { name = custom; dropdownExpanded = false }
                            )
                        }
                    }
                }
            }

            // Intensity slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = intensityColor.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.intensity_label, intensity.toInt()),
                            style = MaterialTheme.typography.titleSmall)
                        Surface(
                            shape = CircleShape,
                            color = intensityColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("${intensity.toInt()}", color = intensityColor,
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Slider(
                        value = intensity, onValueChange = { intensity = it },
                        valueRange = 0f..10f, steps = 9,
                        colors = SliderDefaults.colors(thumbColor = intensityColor, activeTrackColor = intensityColor)
                    )
                }
            }

            // Value field
            OutlinedTextField(
                value = value, onValueChange = { value = it },
                label = { Text(stringResource(R.string.symptom_value_label)) },
                placeholder = { Text(stringResource(R.string.symptom_value_hint)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = duration, onValueChange = { duration = it },
                label = { Text(stringResource(R.string.duration_minutes)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = triggers, onValueChange = { triggers = it },
                label = { Text(stringResource(R.string.triggers_hint)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            // Attachments section
            FilledTonalButton(
                onClick = { filePicker.launch("*/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.attach_file))
            }

            if (attachmentPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    attachmentPaths.forEachIndexed { index, path ->
                        val fileName = File(path).name
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null,
                                    modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(fileName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                                IconButton(onClick = { attachmentPaths = attachmentPaths.toMutableList().also { it.removeAt(index) } },
                                    modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete),
                                        modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addNewSymptom(
                            name = name, intensity = intensity.toInt(),
                            value = value,
                            durationMinutes = duration.toIntOrNull(),
                            triggers = triggers.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            notes = notes, attachmentPaths = attachmentPaths
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
