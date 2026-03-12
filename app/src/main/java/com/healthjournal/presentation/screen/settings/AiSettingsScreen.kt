package com.healthjournal.presentation.screen.settings

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.healthjournal.R
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
    val executionMode by viewModel.executionMode.collectAsStateWithLifecycle()
    val installedModels by viewModel.installedModels.collectAsStateWithLifecycle()
    val activeModel by viewModel.activeModel.collectAsStateWithLifecycle()
    val installProgress by viewModel.installProgress.collectAsStateWithLifecycle()
    val deviceCapability by viewModel.deviceCapability.collectAsStateWithLifecycle()
    val scanResult by viewModel.scanResult.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_settings_title)) },
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
            // AI enabled toggle
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ai_enabled), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.ai_consent_text), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = settings.enabled, onCheckedChange = { viewModel.toggleEnabled(it) })
                }
            }

            AnimatedVisibility(visible = settings.enabled) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // === AI Execution Mode ===
                    ExecutionModeSection(executionMode = executionMode, onModeChange = { viewModel.setExecutionMode(it) })

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
                            config = settings.localAiConfig,
                            onUpdateConfig = { viewModel.updateLocalConfig(it) }
                        )
                    }

                    HorizontalDivider()

                    // Privacy redaction
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(22.dp))
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.ai_privacy_redact), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.ai_privacy_redact_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = settings.privacyRedactEnabled, onCheckedChange = { viewModel.togglePrivacyRedact(it) })
                        }
                    }

                    // Validate button
                    FilledTonalButton(
                        onClick = { viewModel.validateCurrentProvider() },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_validate_config))
                    }

                    // Validation result
                    validationSuccess?.let { isValid ->
                        if (isValid) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
                                    Text(stringResource(R.string.ai_config_valid), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        } else {
                            validationMessage?.let { msg ->
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(20.dp))
                                        Text(msg, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExecutionModeSection(
    executionMode: AiExecutionMode,
    onModeChange: (AiExecutionMode) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.ai_engine_mode), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(stringResource(R.string.ai_engine_mode_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            AiExecutionMode.entries.forEach { mode ->
                val selected = executionMode == mode
                Surface(
                    onClick = { onModeChange(mode) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(selected = selected, onClick = { onModeChange(mode) })
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(mode.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(capability: DeviceCapabilityResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DeviceHub, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.ai_engine_device_info), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.ai_engine_ram, capability.totalRamMb), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.ai_engine_storage, capability.availableStorageMb), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.ai_engine_performance, capability.performanceClass.name), style = MaterialTheme.typography.bodySmall)
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                Text(
                    stringResource(R.string.ai_engine_setup_recommend, capability.recommendedMode.label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

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

    @OptIn(ExperimentalMaterial3Api::class)
    ExposedDropdownMenuBox(expanded = providerMenuExpanded, onExpandedChange = { providerMenuExpanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.ai_select_provider)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = providerMenuExpanded, onDismissRequest = { providerMenuExpanded = false }) {
            providerOptions.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.selectProvider(key)
                        providerMenuExpanded = false
                    },
                    leadingIcon = {
                        Icon(
                            if (key == AiProviderId.CLAUDE.key) Icons.Default.Star else Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
    }

    when (cloudProviderId) {
        AiProviderId.CLAUDE.key -> ClaudeConfigSection(config = settings.claudeConfig, onUpdate = { viewModel.updateClaudeConfig(it) })
        AiProviderId.OPENAI_COMPATIBLE.key -> OpenAiConfigSection(config = settings.openAiConfig, onUpdate = { viewModel.updateOpenAiConfig(it) })
    }
}

@Composable
private fun LocalModelSection(
    viewModel: AiSettingsViewModel,
    installedModels: List<LocalAiModel>,
    activeModel: LocalAiModel?,
    installProgress: InstallProgress?,
    scanResult: Int?,
    config: LocalAiConfig,
    onUpdateConfig: (LocalAiConfig) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.SmartToy, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(stringResource(R.string.local_model_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(stringResource(R.string.local_model_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider()

            // Installed models
            Text(stringResource(R.string.ai_engine_installed_models), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

            if (installedModels.isEmpty()) {
                Text(stringResource(R.string.ai_engine_no_models), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                installedModels.forEach { model ->
                    InstalledModelCard(
                        model = model,
                        isActive = model.modelId == activeModel?.modelId,
                        onActivate = { viewModel.setActiveModel(model.modelId) },
                        onDelete = { viewModel.deleteModel(model.modelId) }
                    )
                }
            }

            // Scan button
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { viewModel.scanForModels() }) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.ai_engine_scan))
                }
                scanResult?.let {
                    Text(stringResource(R.string.ai_engine_scan_result, it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            HorizontalDivider()

            // Model catalog
            Text(stringResource(R.string.ai_engine_model_catalog), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

            val installedIds = installedModels.map { it.modelId }.toSet()
            viewModel.catalogModels.forEach { model ->
                val isInstalled = installedIds.contains(model.modelId)
                val currentProgress = if (installProgress?.modelId == model.modelId) installProgress else null
                CatalogModelCard(
                    model = model,
                    isInstalled = isInstalled,
                    progress = currentProgress,
                    onDownload = { viewModel.downloadModel(model) },
                    compatibility = viewModel.getCompatibilityReport(model)
                )
            }

            // Fallback info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.local_model_fallback_info), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider()

            // Config parameters
            Text(stringResource(R.string.ai_temperature) + ": %.1f".format(config.temperature), style = MaterialTheme.typography.bodySmall)
            Slider(value = config.temperature, onValueChange = { onUpdateConfig(config.copy(temperature = it)) }, valueRange = 0f..1f, steps = 9)
            OutlinedTextField(
                value = config.maxTokens.toString(),
                onValueChange = { onUpdateConfig(config.copy(maxTokens = it.toIntOrNull() ?: 1024)) },
                label = { Text(stringResource(R.string.ai_max_tokens)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
private fun InstalledModelCard(
    model: LocalAiModel,
    isActive: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${model.sizeMb} MB | ${model.quantization ?: model.fileFormat} | ${model.runtimeType}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isActive) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(stringResource(R.string.ai_engine_active), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isActive) {
                    FilledTonalButton(onClick = onActivate, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.ai_engine_activate))
                    }
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = if (isActive) Modifier.fillMaxWidth() else Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.local_model_delete))
                }
            }
        }
    }
}

@Composable
private fun CatalogModelCard(
    model: LocalAiModel,
    isInstalled: Boolean,
    progress: InstallProgress?,
    onDownload: () -> Unit,
    compatibility: CompatibilityReport
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isInstalled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(model.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.ai_engine_size, model.sizeMb) + " | " + stringResource(R.string.ai_engine_ram_required, model.requiredRamMb),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isInstalled) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                } else if (!compatibility.isCompatible) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(stringResource(R.string.ai_engine_incompatible), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            // Warnings
            compatibility.warnings.forEach { warning ->
                Text(warning, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                isInstalled -> { /* already shown in installed section */ }
                progress != null && (progress.state == ModelInstallState.DOWNLOADING || progress.state == ModelInstallState.INSTALLING) -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { progress.progressPercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (progress.state == ModelInstallState.INSTALLING) stringResource(R.string.ai_engine_installing)
                            else stringResource(R.string.ai_engine_downloading, progress.progressPercent),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                progress?.state == ModelInstallState.FAILED -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(16.dp))
                            Text(progress.errorMessage ?: "Failed", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth(), enabled = compatibility.isCompatible) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.local_model_download))
                    }
                }
                else -> {
                    Button(onClick = onDownload, modifier = Modifier.fillMaxWidth(), enabled = compatibility.isCompatible) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.local_model_download))
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaudeConfigSection(config: ClaudeConfig, onUpdate: (ClaudeConfig) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = config.apiKey, onValueChange = { onUpdate(config.copy(apiKey = it)) },
                label = { Text(stringResource(R.string.ai_api_key)) }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), singleLine = true)
            OutlinedTextField(value = config.model, onValueChange = { onUpdate(config.copy(model = it)) },
                label = { Text(stringResource(R.string.ai_model)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = config.baseUrl, onValueChange = { onUpdate(config.copy(baseUrl = it)) },
                label = { Text(stringResource(R.string.ai_base_url)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = config.timeoutSeconds.toString(),
                onValueChange = { onUpdate(config.copy(timeoutSeconds = it.toIntOrNull() ?: 60)) },
                label = { Text(stringResource(R.string.ai_timeout)) }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        }
    }
}

@Composable
private fun OpenAiConfigSection(config: OpenAiConfig, onUpdate: (OpenAiConfig) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = config.apiKey, onValueChange = { onUpdate(config.copy(apiKey = it)) },
                label = { Text(stringResource(R.string.ai_api_key)) }, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(), singleLine = true)
            OutlinedTextField(value = config.model, onValueChange = { onUpdate(config.copy(model = it)) },
                label = { Text(stringResource(R.string.ai_model)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = config.baseUrl, onValueChange = { onUpdate(config.copy(baseUrl = it)) },
                label = { Text(stringResource(R.string.ai_base_url)) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        }
    }
}
