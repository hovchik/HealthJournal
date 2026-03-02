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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.domain.model.VitalType
import com.healthjournal.util.AttachmentHelper
import com.healthjournal.util.localizedDisplayName
import com.healthjournal.util.localizedUnit
import kotlinx.coroutines.flow.collectLatest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddVitalScreen(
    onBack: () -> Unit,
    vitalId: Long = -1L,
    diseaseId: Long = 0L,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(VitalType.BLOOD_PRESSURE) }
    var value by remember { mutableStateOf("") }
    var secondaryValue by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var attachmentPaths by remember { mutableStateOf(listOf<String>()) }
    var loaded by remember { mutableStateOf(vitalId == -1L) }

    val isEditing = vitalId != -1L
    var editingVital by remember { mutableStateOf<com.healthjournal.domain.model.VitalSign?>(null) }

    LaunchedEffect(vitalId) {
        if (vitalId != -1L) {
            val vital = viewModel.getVitalSignById(vitalId)
            if (vital != null) {
                editingVital = vital
                selectedType = vital.type
                value = vital.value.toString()
                secondaryValue = vital.secondaryValue?.toString() ?: ""
                notes = vital.notes
                attachmentPaths = vital.attachmentPaths
            }
            loaded = true
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        val paths = uris.mapNotNull { uri -> AttachmentHelper.copyToInternal(context, uri) }
        attachmentPaths = attachmentPaths + paths
    }

    LaunchedEffect(Unit) { viewModel.saveSuccess.collectLatest { onBack() } }

    if (!loaded) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (isEditing) R.string.edit_vital_title else R.string.add_vital_title))
                },
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
            // Vital type selector
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
                    leadingIcon = { Icon(Icons.Default.MonitorHeart, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.tertiary) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    VitalType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.localizedDisplayName()) },
                            onClick = { selectedType = type; typeMenuExpanded = false }
                        )
                    }
                }
            }

            // Value inputs
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = value, onValueChange = { value = it },
                        label = {
                            Text(if (selectedType == VitalType.BLOOD_PRESSURE) stringResource(R.string.systolic_upper)
                                else stringResource(R.string.value_with_unit, selectedType.localizedUnit()))
                        },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    if (selectedType == VitalType.BLOOD_PRESSURE) {
                        OutlinedTextField(
                            value = secondaryValue, onValueChange = { secondaryValue = it },
                            label = { Text(stringResource(R.string.diastolic_lower)) },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text(stringResource(R.string.notes)) },
                modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            // Attachments
            FilledTonalButton(onClick = { filePicker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.attach_file))
            }

            if (attachmentPaths.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    attachmentPaths.forEachIndexed { index, path ->
                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null,
                                    modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(File(path).name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
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
                    val v = value.toDoubleOrNull()
                    if (v != null) {
                        if (isEditing && editingVital != null) {
                            viewModel.updateVitalSign(
                                editingVital!!.copy(
                                    type = selectedType,
                                    value = v,
                                    secondaryValue = secondaryValue.toDoubleOrNull(),
                                    notes = notes,
                                    attachmentPaths = attachmentPaths
                                )
                            )
                        } else {
                            viewModel.addNewVitalSign(
                                type = selectedType, value = v, secondaryValue = secondaryValue.toDoubleOrNull(),
                                notes = notes, attachmentPaths = attachmentPaths,
                                diseaseId = diseaseId
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = value.toDoubleOrNull() != null
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}
