package com.healthjournal.presentation.screen.settings

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.HealthJournalApp
import com.healthjournal.R
import com.healthjournal.domain.model.VitalType
import com.healthjournal.util.HealthConnectSyncWorker
import com.healthjournal.util.localizedDisplayName
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

    private val _hasPermissions = MutableStateFlow(false)
    val hasPermissions = _hasPermissions.asStateFlow()

    private val _autoSyncEnabled = MutableStateFlow(
        HealthConnectSyncWorker.isEnabled(application)
    )
    val autoSyncEnabled = _autoSyncEnabled.asStateFlow()

    private val _syncIntervalHours = MutableStateFlow(
        HealthConnectSyncWorker.getSyncInterval(application)
    )
    val syncIntervalHours = _syncIntervalHours.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _hasPermissions.value = try {
                healthConnectManager.hasAllPermissions()
            } catch (_: Exception) {
                false
            }
        }
    }

    fun importData(days: Int = 7) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
                val vitals = if (days == 1) {
                    healthConnectManager.importRecent(profileId, days = 1)
                } else {
                    healthConnectManager.importAllRecent(profileId)
                }
                var imported = 0
                for (vital in vitals) {
                    val id = container.addVitalSignIfNotDuplicate(vital)
                    if (id != null) imported++
                }
                _importedCount.value = imported
                _message.value = getApplication<Application>().getString(R.string.import_success_count, imported)
            } catch (e: Exception) {
                _message.value = getApplication<Application>().getString(R.string.import_error, e.message ?: "")
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun setAutoSync(enabled: Boolean) {
        val context = getApplication<Application>()
        val interval = _syncIntervalHours.value
        HealthConnectSyncWorker.setEnabled(context, enabled, interval)
        _autoSyncEnabled.value = enabled
    }

    fun setSyncInterval(hours: Long) {
        val context = getApplication<Application>()
        HealthConnectSyncWorker.setSyncInterval(context, hours)
        _syncIntervalHours.value = hours
    }

    fun clearMessage() {
        _message.value = null
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
    val hasPermissions by viewModel.hasPermissions.collectAsStateWithLifecycle()
    val isAvailable = viewModel.healthConnectManager.isAvailable
    val autoSyncEnabled by viewModel.autoSyncEnabled.collectAsStateWithLifecycle()
    val syncIntervalHours by viewModel.syncIntervalHours.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.checkPermissions()
    }

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

            // Permissions card
            if (isAvailable && !hasPermissions) {
                ElevatedCard(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.HealthAndSafety,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.health_connect_permissions_needed),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            stringResource(R.string.health_connect_permissions_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(viewModel.healthConnectManager.requiredPermissions)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.health_connect_grant_permissions))
                        }
                    }
                }
            }

            // Auto-sync scheduler card
            if (isAvailable && hasPermissions) {
                ElevatedCard(shape = MaterialTheme.shapes.large) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.health_connect_auto_sync),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.health_connect_auto_sync_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoSyncEnabled,
                                onCheckedChange = { viewModel.setAutoSync(it) }
                            )
                        }

                        if (autoSyncEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.health_connect_sync_interval),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val intervals = listOf(1L, 3L, 6L, 12L, 24L)
                                intervals.forEachIndexed { index, hours ->
                                    SegmentedButton(
                                        selected = syncIntervalHours == hours,
                                        onClick = { viewModel.setSyncInterval(hours) },
                                        shape = SegmentedButtonDefaults.itemShape(index, intervals.size)
                                    ) {
                                        Text(
                                            stringResource(R.string.health_connect_hours_format, hours),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
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
                            Text(type.localizedDisplayName(), style = MaterialTheme.typography.bodyMedium)
                        }
                        if (index < dataTypes.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            // Import buttons
            Button(
                onClick = { viewModel.importData(days = 1) },
                enabled = isAvailable && hasPermissions && !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.health_connect_import_1_day))
            }

            OutlinedButton(
                onClick = { viewModel.importData(days = 7) },
                enabled = isAvailable && hasPermissions && !isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.health_connect_import))
            }

            if (!hasPermissions && isAvailable) {
                Text(
                    stringResource(R.string.health_connect_permissions_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            message?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.startsWith("Error")) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            msg,
                            modifier = Modifier.weight(1f),
                            color = if (msg.startsWith("Error")) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = { viewModel.clearMessage() }) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        }
    }
}
