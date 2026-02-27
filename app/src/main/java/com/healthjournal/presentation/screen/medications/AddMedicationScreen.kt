package com.healthjournal.presentation.screen.medications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.util.PredefinedData
import com.healthjournal.util.PredefinedDataKeys
import com.healthjournal.util.predefinedDataStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicationScreen(
    onBack: () -> Unit,
    viewModel: MedicationsViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val disabledMedications by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.DISABLED_MEDICATIONS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())
    val customMedications by remember {
        context.predefinedDataStore.data.map { prefs ->
            prefs[PredefinedDataKeys.CUSTOM_MEDICATIONS] ?: emptySet()
        }
    }.collectAsState(initial = emptySet())

    val enabledPredefined = PredefinedData.medications.filter { it.key !in disabledMedications }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_medication_title)) },
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
                label = { Text(stringResource(R.string.medication_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                stringResource(R.string.common_medications),
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
                customMedications.forEach { custom ->
                    SuggestionChip(
                        onClick = { name = custom },
                        label = { Text(custom) }
                    )
                }
            }

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text(stringResource(R.string.dosage_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it },
                label = { Text(stringResource(R.string.schedule_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    if (name.isNotBlank() && dosage.isNotBlank()) {
                        viewModel.addNewMedication(name, dosage, frequency, notes)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && dosage.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
