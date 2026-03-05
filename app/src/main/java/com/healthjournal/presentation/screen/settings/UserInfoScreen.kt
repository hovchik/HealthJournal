package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sick
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.HealthJournalApp
import com.healthjournal.R
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as HealthJournalApp).container.userSettingsRepository

    val settings = repo.getUserSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun save(
        userName: String,
        doctorName: String,
        doctorPhone: String,
        weight: String,
        height: String,
        knownDiseases: List<String>
    ) {
        viewModelScope.launch {
            settings.value?.let { current ->
                repo.updateUserSettings(
                    current.copy(
                        userName = userName,
                        doctorName = doctorName,
                        doctorPhone = doctorPhone,
                        weight = weight,
                        height = height,
                        knownDiseases = knownDiseases
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun UserInfoScreen(
    onBack: () -> Unit,
    viewModel: UserInfoViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var userName by remember(settings) { mutableStateOf(settings?.userName ?: "") }
    var doctorName by remember(settings) { mutableStateOf(settings?.doctorName ?: "") }
    var doctorPhone by remember(settings) { mutableStateOf(settings?.doctorPhone ?: "") }
    var weight by remember(settings) { mutableStateOf(settings?.weight ?: "") }
    var height by remember(settings) { mutableStateOf(settings?.height ?: "") }
    var knownDiseases by remember(settings) { mutableStateOf(settings?.knownDiseases ?: emptyList()) }
    var newDisease by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.user_info_title)) },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        if (userName.isNotBlank()) userName else stringResource(R.string.your_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            OutlinedTextField(
                value = userName,
                onValueChange = { userName = it; saved = false },
                label = { Text(stringResource(R.string.your_name)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Weight & Height
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it; saved = false },
                    label = { Text(stringResource(R.string.user_weight)) },
                    leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it; saved = false },
                    label = { Text(stringResource(R.string.user_height)) },
                    leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it; saved = false },
                label = { Text(stringResource(R.string.doctor_name)) },
                leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = doctorPhone,
                onValueChange = { doctorPhone = it; saved = false },
                label = { Text(stringResource(R.string.doctor_phone)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Known diseases section
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Sick, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                        Text(
                            stringResource(R.string.known_diseases_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        stringResource(R.string.known_diseases_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (knownDiseases.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            knownDiseases.forEach { disease ->
                                InputChip(
                                    selected = false,
                                    onClick = {},
                                    label = { Text(disease) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                knownDiseases = knownDiseases - disease
                                                saved = false
                                            },
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDisease,
                            onValueChange = { newDisease = it },
                            label = { Text(stringResource(R.string.add_known_disease)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        FilledTonalIconButton(
                            onClick = {
                                if (newDisease.isNotBlank()) {
                                    knownDiseases = knownDiseases + newDisease.trim()
                                    newDisease = ""
                                    saved = false
                                }
                            },
                            enabled = newDisease.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_disease))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.save(userName, doctorName, doctorPhone, weight, height, knownDiseases)
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }

            if (saved) {
                Text(
                    stringResource(R.string.saved_successfully),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
