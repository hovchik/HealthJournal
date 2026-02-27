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
import com.healthjournal.domain.model.VitalType
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVitalScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var selectedType by remember { mutableStateOf(VitalType.BLOOD_PRESSURE) }
    var value by remember { mutableStateOf("") }
    var secondaryValue by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { onBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_vital_title)) },
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
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedType.localizedDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.vital_type_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false }
                ) {
                    VitalType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.localizedDisplayName()) },
                            onClick = {
                                selectedType = type
                                typeMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = {
                    Text(
                        if (selectedType == VitalType.BLOOD_PRESSURE) stringResource(R.string.systolic_upper)
                        else stringResource(R.string.value_with_unit, selectedType.localizedUnit())
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (selectedType == VitalType.BLOOD_PRESSURE) {
                OutlinedTextField(
                    value = secondaryValue,
                    onValueChange = { secondaryValue = it },
                    label = { Text(stringResource(R.string.diastolic_lower)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Button(
                onClick = {
                    val v = value.toDoubleOrNull()
                    if (v != null) {
                        viewModel.addNewVitalSign(
                            type = selectedType,
                            value = v,
                            secondaryValue = secondaryValue.toDoubleOrNull(),
                            notes = notes
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = value.toDoubleOrNull() != null
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
