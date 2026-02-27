package com.healthjournal.presentation.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSymptomScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var intensity by remember { mutableFloatStateOf(5f) }
    var duration by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addNewSymptom(
                            name = name,
                            intensity = intensity.toInt(),
                            durationMinutes = duration.toIntOrNull(),
                            triggers = triggers.split(",").map { it.trim() }.filter { it.isNotBlank() },
                            notes = notes
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
