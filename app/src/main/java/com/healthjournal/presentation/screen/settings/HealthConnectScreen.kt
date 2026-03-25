package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.HealthJournalApp
import com.healthjournal.R
import com.healthjournal.domain.model.VitalType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HealthConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    val healthConnectManager = container.healthConnectManager

    private val _importedCount = MutableStateFlow(0)
    val importedCount = _importedCount.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting = _isImporting.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    fun importData() {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
                val vitals = healthConnectManager.importAllRecent(profileId)
                for (vital in vitals) {
                    container.addVitalSign(vital)
                }
                _importedCount.value = vitals.size
                _message.value = "Imported ${vitals.size} records"
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isImporting.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(
    onBack: () -> Unit,
    viewModel: HealthConnectViewModel = viewModel()
) {
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importedCount by viewModel.importedCount.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val isAvailable = viewModel.healthConnectManager.isAvailable

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.health_connect_title), fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAvailable) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Watch,
                                contentDescription = null,
                                tint = if (isAvailable) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.health_connect_status),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (isAvailable) stringResource(R.string.health_connect_available)
                            else stringResource(R.string.health_connect_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAvailable) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Supported data types
            Text(stringResource(R.string.health_connect_data_types), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            val dataTypes = listOf(
                VitalType.PULSE to Icons.Default.Favorite,
                VitalType.STEPS to Icons.Default.DirectionsWalk,
                VitalType.SLEEP to Icons.Default.Bedtime,
                VitalType.SPO2 to Icons.Default.Air,
                VitalType.BLOOD_PRESSURE to Icons.Default.MonitorHeart,
                VitalType.WEIGHT to Icons.Default.FitnessCenter
            )

            ElevatedCard(shape = MaterialTheme.shapes.large) {
                Column {
                    dataTypes.forEachIndexed { index, (type, icon) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(type.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                        if (index < dataTypes.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // Import button
            Button(
                onClick = { viewModel.importData() },
                enabled = isAvailable && !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.health_connect_import))
            }

            message?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        msg,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
