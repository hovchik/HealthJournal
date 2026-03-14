package com.healthjournal.presentation.screen.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
import com.healthjournal.data.ai.ModelCatalog
import com.healthjournal.domain.model.ai.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    onBack: () -> Unit,
    viewModel: AiSettingsViewModel = viewModel()
) {
    val settings by viewModel.aiSettings.collectAsStateWithLifecycle()
    val validationMessage by viewModel.validationMessage.collectAsStateWithLifecycle()
    val validationSuccess by viewModel.validationSuccess.collectAsStateWithLifecycle()
    val validatingInProgress by viewModel.validatingInProgress.collectAsStateWithLifecycle()
    val executionMode by viewModel.executionMode.collectAsStateWithLifecycle()
    val installedModels by viewModel.installedModels.collectAsStateWithLifecycle()
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val installProgress by viewModel.installProgress.collectAsStateWithLifecycle()
    val deviceCapability by viewModel.deviceCapability.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()
    val downloadError by viewModel.downloadError.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
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
            // AI enabled toggle
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.ai_enabled),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.ai_consent_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { viewModel.toggleEnabled(it) }
                    )
                }
            }

            AnimatedVisibility(visible = settings.enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // === AI Execution Mode ===
                    ExecutionModeSection(
                        executionMode = executionMode,
                        onModeChange = { viewModel.setExecutionMode(it) }
                    )

                    // === Device Info ===
                    deviceCapability?.let { DeviceInfoCard(it) }

                    // === Cloud provider config (when CLOUD mode) ===
                    if (executionMode == AiExecutionMode.CLOUD) {
                        CloudProviderSection(
                            settings = settings,
                            viewModel = viewModel
                        )
                    }

                    // === Local Model Section (when CUSTOM_LOCAL or AUTO) ===
                    if (executionMode == AiExecutionMode.CUSTOM_LOCAL || executionMode == AiExecutionMode.AUTO) {
                        LocalModelSection(
                            viewModel = viewModel,
                            installedModels = installedModels,
                            activeModel = activeModel,
                            installProgress = installProgress,
                            scanResult = scanResult,
                            scanProgress = scanProgress,
                            downloadError = downloadError
                        )
                    }

                    HorizontalDivider()

                    // Privacy redaction
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(R.string.ai_privacy_redact),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.ai_privacy_redact_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.privacyRedactEnabled,
                                onCheckedChange = { viewModel.togglePrivacyRedact(it) }
                            )
                        }
                    }

                    // Validate button
                    ValidateConfigSection(
                        executionMode = executionMode,
                        validatingInProgress = validatingInProgress,
                        validationSuccess = validationSuccess,
                        validationMessage = validationMessage,
                        onValidate = { viewModel.validateCurrentProvider() }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Execution Mode Section
// ---------------------------------------------------------------------------

@Composable
private fun ExecutionModeSection(
    executionMode: AiExecutionMode,
    onModeChange: (AiExecutionMode) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.ai_engine_mode),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                stringResource(R.string.ai_engine_mode_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AiExecutionMode.entries.forEach { mode ->
                val selected = executionMode == mode
                Surface(
                    onClick = { onModeChange(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { onModeChange(mode) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                mode.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                mode.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Device Info Card (Fixed)
// ---------------------------------------------------------------------------

@Composable
private fun DeviceInfoCard(capability: DeviceCapabilityResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.DeviceHub,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.ai_engine_device_info),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Android version
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    stringResource(R.string.ai_engine_android_version,
                        capability.androidVersion, capability.sdkInt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // RAM, Storage, Performance in a clear grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DeviceInfoItem(
                    icon = Icons.Default.Memory,
                    label = "RAM",
                    value = formatSize(capability.totalRamMb)
                )
                DeviceInfoItem(
                    icon = Icons.Default.Storage,
                    label = stringResource(R.string.ai_engine_storage),
                    value = formatSize(capability.availableStorageMb)
                )
                DeviceInfoItem(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.ai_engine_performance),
                    value = capability.performanceClass.name
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // CPU architecture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DeveloperBoard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    stringResource(R.string.ai_engine_cpu_arch,
                        capability.supportedAbis.joinToString(", ")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // AICore status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (capability.aiCoreAvailable) Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (capability.aiCoreAvailable)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    if (capability.aiCoreAvailable)
                        stringResource(R.string.ai_engine_aicore_available) +
                            (capability.aiCoreVersion?.let { " (v$it)" } ?: "")
                    else stringResource(R.string.ai_engine_aicore_not_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Recommended mode badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Recommend,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        stringResource(R.string.ai_engine_recommended_mode,
                            capability.recommendedMode.label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ---------------------------------------------------------------------------
// Cloud Provider Section
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudProviderSection(
    settings: AiSettings,
    viewModel: AiSettingsViewModel
) {
    val providerOptions = listOf(
        AiProviderId.CLAUDE.key to stringResource(R.string.ai_provider_claude),
        AiProviderId.OPENAI_COMPATIBLE.key to stringResource(R.string.ai_provider_openai)
    )
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val cloudProviderId = if (settings.selectedProviderId == AiProviderId.LOCAL.key)
        AiProviderId.CLAUDE.key else settings.selectedProviderId
    val selectedLabel = providerOptions.firstOrNull { it.first == cloudProviderId }?.second
        ?: stringResource(R.string.ai_provider_claude)

    ExposedDropdownMenuBox(
        expanded = providerMenuExpanded,
        onExpandedChange = { providerMenuExpanded = it }
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.ai_select_provider)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded)
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = providerMenuExpanded,
            onDismissRequest = { providerMenuExpanded = false }
        ) {
            providerOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.selectProvider(key)
                        providerMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            if (key == AiProviderId.CLAUDE.key) Icons.Default.Star
                            else Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }

    when (cloudProviderId) {
        AiProviderId.CLAUDE.key -> ClaudeConfigSection(
            config = settings.claudeConfig,
            onUpdate = { viewModel.updateClaudeConfig(it) }
        )
        AiProviderId.OPENAI_COMPATIBLE.key -> OpenAiConfigSection(
            config = settings.openAiConfig,
            onUpdate = { viewModel.updateOpenAiConfig(it) }
        )
    }
}

// ---------------------------------------------------------------------------
// Local Model Section
// ---------------------------------------------------------------------------

@Composable
private fun LocalModelSection(
    viewModel: AiSettingsViewModel,
    installedModels: List<LocalAiModel>,
    activeModel: LocalAiModel?,
    installProgress: InstallProgress?,
    scanResult: Int?,
    scanProgress: ScanProgress?,
    downloadError: String?
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.local_model_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                stringResource(R.string.local_model_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // Installed models
            Text(
                stringResource(R.string.ai_engine_installed_models),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            val actuallyInstalled = installedModels.filter {
                it.installState == ModelInstallState.INSTALLED
            }

            if (actuallyInstalled.isEmpty()) {
                Text(
                    stringResource(R.string.ai_engine_no_models),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                actuallyInstalled.forEach { model ->
                    InstalledModelCard(
                        model = model,
                        isActive = model.modelId == activeModel?.modelId,
                        onActivate = { viewModel.setActiveModel(model.modelId) },
                        onDelete = { viewModel.deleteModel(model.modelId) }
                    )
                }
            }

            // Scan button with progress and permission handling
            val context = LocalContext.current
            val isScanning = scanProgress?.isScanning == true

            var hasStoragePermission by remember {
                mutableStateOf(checkStoragePermission(context))
            }

            val storagePermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { granted ->
                hasStoragePermission = granted
                if (granted) viewModel.scanForModels()
            }

            val manageStorageLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                hasStoragePermission = checkStoragePermission(context)
                if (hasStoragePermission) viewModel.scanForModels()
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Permission warning when not granted
                if (!hasStoragePermission) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                stringResource(R.string.storage_permission_required),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalButton(
                                onClick = {
                                    requestStoragePermission(
                                        context = context,
                                        legacyLauncher = { storagePermissionLauncher.launch(it) },
                                        manageLauncher = { manageStorageLauncher.launch(it) }
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    stringResource(R.string.storage_permission_grant),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (hasStoragePermission) {
                                viewModel.scanForModels()
                            } else {
                                requestStoragePermission(
                                    context = context,
                                    legacyLauncher = { storagePermissionLauncher.launch(it) },
                                    manageLauncher = { manageStorageLauncher.launch(it) }
                                )
                            }
                        },
                        enabled = !isScanning
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isScanning) stringResource(R.string.ai_engine_scanning)
                            else stringResource(R.string.ai_engine_scan)
                        )
                    }
                    if (!isScanning) {
                        scanResult?.let {
                            Text(
                                stringResource(R.string.ai_engine_scan_result, it),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Scan progress details
                if (isScanning && scanProgress != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (scanProgress.totalFolders > 0) {
                            LinearProgressIndicator(
                                progress = {
                                    scanProgress.foldersScanned.toFloat() / scanProgress.totalFolders
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            stringResource(R.string.ai_engine_scan_folders) +
                                ": ${scanProgress.currentFolder}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (scanProgress.modelsFound > 0) {
                            Text(
                                stringResource(R.string.ai_engine_scan_result, scanProgress.modelsFound),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // Model catalog
            Text(
                stringResource(R.string.ai_engine_model_catalog),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            val installedIds = remember(installedModels) {
                installedModels
                    .filter { it.installState == ModelInstallState.INSTALLED }
                    .map { it.modelId }
                    .toSet()
            }

            viewModel.catalogModels.forEach { model ->
                val isInstalled = model.modelId in installedIds
                val currentProgress = installProgress?.takeIf { it.modelId == model.modelId }
                val compatibility = remember(model.modelId) {
                    viewModel.getCompatibilityReport(model)
                }
                val hasDownloadError = downloadError == model.modelId

                CatalogModelCard(
                    model = model,
                    isInstalled = isInstalled,
                    progress = currentProgress,
                    onDownload = { viewModel.downloadModel(model) },
                    onPause = { viewModel.pauseDownload() },
                    compatibilityMessage = compatibility,
                    hasNoDownloadUrl = hasDownloadError,
                    onDismissError = { viewModel.clearDownloadError() }
                )
            }

            // Fallback info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        stringResource(R.string.local_model_fallback_info),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Supported formats info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            stringResource(R.string.ai_engine_supported_formats),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        ModelCatalog.supportedFormats.joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Installed Model Card
// ---------------------------------------------------------------------------

@Composable
private fun InstalledModelCard(
    model: LocalAiModel,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        model.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        buildString {
                            append(formatSize(model.sizeMb))
                            append(" | ")
                            append(model.fileFormat.uppercase())
                            model.quantization?.let { append(" ($it)") }
                            append(" | ")
                            append(model.runtimeType)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            stringResource(R.string.ai_engine_active),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isActive) {
                    FilledTonalButton(
                        onClick = onActivate,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.ai_engine_activate))
                    }
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = if (isActive) Modifier.fillMaxWidth() else Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.local_model_delete))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Catalog Model Card
// ---------------------------------------------------------------------------

@Composable
private fun CatalogModelCard(
    model: LocalAiModel,
    isInstalled: Boolean,
    progress: InstallProgress?,
    onDownload: () -> Unit,
    onPause: () -> Unit = {},
    compatibilityMessage: String?,
    hasNoDownloadUrl: Boolean = false,
    onDismissError: () -> Unit = {}
) {
    val isCompatible = compatibilityMessage == null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else
                MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        model.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        stringResource(R.string.ai_engine_size, model.sizeMb) + " | " +
                            stringResource(R.string.ai_engine_ram_required, model.requiredRamMb),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    model.quantization?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (isInstalled) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (!isCompatible) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            stringResource(R.string.ai_engine_incompatible),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Compatibility warning message
            if (!isCompatible && compatibilityMessage != null) {
                Text(
                    compatibilityMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // No download URL error
            if (hasNoDownloadUrl) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            stringResource(R.string.ai_engine_no_download_url),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isInstalled -> {
                    // Already shown in installed section - no action needed
                }
                progress != null && progress.state == ModelInstallState.DOWNLOADING -> {
                    Column {
                        if (progress.isPaused) {
                            LinearProgressIndicator(
                                progress = { progress.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.outline
                            )
                        } else {
                            LinearProgressIndicator(
                                progress = { progress.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (progress.isPaused) {
                                    Text(
                                        stringResource(R.string.ai_engine_download_paused),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                } else {
                                    Text(
                                        stringResource(R.string.ai_engine_downloading, progress.progressPercent),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (progress.totalBytes > 0) {
                                    val downloaded = formatFileSize(progress.bytesDownloaded)
                                    val total = formatFileSize(progress.totalBytes)
                                    val speedText = if (progress.bytesPerSecond > 0 && !progress.isPaused) {
                                        " \u2022 " + stringResource(R.string.ai_engine_download_speed, formatFileSize(progress.bytesPerSecond))
                                    } else ""
                                    Text(
                                        stringResource(R.string.ai_engine_download_progress, downloaded, total) + speedText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            if (progress.isPaused) {
                                FilledTonalButton(
                                    onClick = onDownload,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.ai_engine_download_resume), style = MaterialTheme.typography.labelSmall)
                                }
                            } else {
                                FilledTonalButton(
                                    onClick = onPause,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.ai_engine_download_pause), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                progress != null && progress.state == ModelInstallState.INSTALLING -> {
                    Column {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.ai_engine_installing),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                progress?.state == ModelInstallState.FAILED -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                progress.errorMessage ?: "Failed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isCompatible
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.ai_engine_download_retry))
                    }
                }
                else -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isCompatible
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.local_model_download))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Validate Configuration Section (Improved)
// ---------------------------------------------------------------------------

@Composable
private fun ValidateConfigSection(
    executionMode: AiExecutionMode,
    validatingInProgress: Boolean,
    validationSuccess: Boolean?,
    validationMessage: String?,
    onValidate: () -> Unit
) {
    // Current mode indicator
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                stringResource(R.string.ai_validate_mode_info, executionMode.label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Validate button with loading state
    FilledTonalButton(
        onClick = onValidate,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        enabled = !validatingInProgress
    ) {
        if (validatingInProgress) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.ai_validate_checking))
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.ai_validate_config))
        }
    }

    // Validation result
    validationSuccess?.let { isValid ->
        if (isValid) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        stringResource(R.string.ai_config_valid),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        validationMessage
                            ?: when (executionMode) {
                                AiExecutionMode.CUSTOM_LOCAL ->
                                    stringResource(R.string.ai_validate_local_no_model)
                                else -> stringResource(R.string.ai_api_key_required)
                            },
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cloud Config Sections
// ---------------------------------------------------------------------------

@Composable
private fun ClaudeConfigSection(
    config: ClaudeConfig,
    onUpdate: (ClaudeConfig) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = config.apiKey,
                onValueChange = { onUpdate(config.copy(apiKey = it)) },
                label = { Text(stringResource(R.string.ai_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.model,
                onValueChange = { onUpdate(config.copy(model = it)) },
                label = { Text(stringResource(R.string.ai_model)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.baseUrl,
                onValueChange = { onUpdate(config.copy(baseUrl = it)) },
                label = { Text(stringResource(R.string.ai_base_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.timeoutSeconds.toString(),
                onValueChange = { onUpdate(config.copy(timeoutSeconds = it.toIntOrNull() ?: 60)) },
                label = { Text(stringResource(R.string.ai_timeout)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
private fun OpenAiConfigSection(
    config: OpenAiConfig,
    onUpdate: (OpenAiConfig) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = config.apiKey,
                onValueChange = { onUpdate(config.copy(apiKey = it)) },
                label = { Text(stringResource(R.string.ai_api_key)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.model,
                onValueChange = { onUpdate(config.copy(model = it)) },
                label = { Text(stringResource(R.string.ai_model)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = config.baseUrl,
                onValueChange = { onUpdate(config.copy(baseUrl = it)) },
                label = { Text(stringResource(R.string.ai_base_url)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatSize(mb: Long): String {
    return if (mb >= 1024) "%.1f GB".format(mb / 1024.0) else "$mb MB"
}

private fun checkStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PermissionChecker.PERMISSION_GRANTED
    }
}

private fun requestStoragePermission(
    context: android.content.Context,
    legacyLauncher: (String) -> Unit,
    manageLauncher: (Intent) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        manageLauncher(intent)
    } else {
        legacyLauncher(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "%.0f KB".format(kb)
    val mb = kb / 1024.0
    if (mb < 1024) return "%.1f MB".format(mb)
    val gb = mb / 1024.0
    return "%.2f GB".format(gb)
}
