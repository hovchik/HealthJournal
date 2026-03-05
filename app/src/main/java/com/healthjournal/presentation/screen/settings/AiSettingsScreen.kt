package com.healthjournal.presentation.screen.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

    val providerOptions = listOf(
        AiProviderId.GEMINI_NANO.key to stringResource(R.string.ai_provider_system),
        AiProviderId.CLAUDE.key to stringResource(R.string.ai_provider_claude),
        AiProviderId.OPENAI_COMPATIBLE.key to stringResource(R.string.ai_provider_openai),
        AiProviderId.LOCAL.key to stringResource(R.string.ai_provider_local)
    )
    var providerMenuExpanded by remember { mutableStateOf(false) }
    val selectedProviderLabel = providerOptions.firstOrNull { it.first == settings.selectedProviderId }?.second
        ?: stringResource(R.string.ai_provider_system)

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
                    // Provider selector dropdown
                    ExposedDropdownMenuBox(
                        expanded = providerMenuExpanded,
                        onExpandedChange = { providerMenuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProviderLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.ai_select_provider)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = providerMenuExpanded,
                            onDismissRequest = { providerMenuExpanded = false }
                        ) {
                            providerOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label)
                                            if (key == AiProviderId.GEMINI_NANO.key) {
                                                Surface(
                                                    shape = MaterialTheme.shapes.extraSmall,
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        stringResource(R.string.system_ai_recommended),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            if (key == AiProviderId.LOCAL.key) {
                                                Surface(
                                                    shape = MaterialTheme.shapes.extraSmall,
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        stringResource(R.string.ai_local_analysis_always_available),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectProvider(key)
                                        providerMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            when (key) {
                                                AiProviderId.GEMINI_NANO.key -> Icons.Default.PhoneAndroid
                                                AiProviderId.CLAUDE.key -> Icons.Default.Star
                                                AiProviderId.OPENAI_COMPATIBLE.key -> Icons.Default.Cloud
                                                else -> Icons.Default.Folder
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Provider-specific config
                    when (settings.selectedProviderId) {
                        AiProviderId.GEMINI_NANO.key -> GeminiNanoConfigSection(
                            config = settings.geminiNanoConfig,
                            onUpdate = { viewModel.updateGeminiNanoConfig(it) }
                        )
                        AiProviderId.CLAUDE.key -> ClaudeConfigSection(
                            config = settings.claudeConfig,
                            onUpdate = { viewModel.updateClaudeConfig(it) }
                        )
                        AiProviderId.OPENAI_COMPATIBLE.key -> OpenAiConfigSection(
                            config = settings.openAiConfig,
                            onUpdate = { viewModel.updateOpenAiConfig(it) }
                        )
                        AiProviderId.LOCAL.key -> LocalAnalysisInfoCard()
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
                    FilledTonalButton(
                        onClick = { viewModel.validateCurrentProvider() },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.ai_validate_config))
                    }

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
                            validationMessage?.let { msg ->
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
                                            msg,
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
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
private fun GeminiNanoConfigSection(
    config: GeminiNanoConfig,
    onUpdate: (GeminiNanoConfig) -> Unit
) {
    val context = LocalContext.current

    // Check AI Core package availability
    var aiCoreInstalled by remember {
        mutableStateOf(
            try { context.packageManager.getPackageInfo("com.google.android.aicore", 0); true }
            catch (_: PackageManager.NameNotFoundException) { false }
        )
    }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var statusIsPositive by remember { mutableStateOf(false) }

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
            Text(
                stringResource(R.string.gemini_nano_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // AI Core status card
            if (!aiCoreInstalled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                stringResource(R.string.gemini_nano_not_installed),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                        "market://details?id=com.google.android.aicore"
                                    ))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                                        "https://play.google.com/store/apps/details?id=com.google.android.aicore"
                                    ))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.GetApp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.gemini_nano_install))
                        }
                    }
                }
            }

            // Check/refresh status button
            OutlinedButton(
                onClick = {
                    aiCoreInstalled = try {
                        context.packageManager.getPackageInfo("com.google.android.aicore", 0); true
                    } catch (_: PackageManager.NameNotFoundException) { false }

                    if (aiCoreInstalled) {
                        // Also check Samsung AI packages
                        val hasSamsungAi = listOf(
                            "com.samsung.android.aicoreondevice",
                            "com.samsung.android.galaxyai",
                            "com.samsung.android.intelligence"
                        ).any { pkg ->
                            try { context.packageManager.getPackageInfo(pkg, 0); true }
                            catch (_: Exception) { false }
                        }
                        statusMessage = if (hasSamsungAi) {
                            context.getString(R.string.gemini_nano_status_available) + " + Samsung Galaxy AI"
                        } else {
                            context.getString(R.string.gemini_nano_status_available)
                        }
                        statusIsPositive = true
                    } else {
                        // Check Samsung AI as alternative
                        val hasSamsungAi = listOf(
                            "com.samsung.android.aicoreondevice",
                            "com.samsung.android.galaxyai",
                            "com.samsung.android.intelligence"
                        ).any { pkg ->
                            try { context.packageManager.getPackageInfo(pkg, 0); true }
                            catch (_: Exception) { false }
                        }
                        if (hasSamsungAi) {
                            statusMessage = "Samsung Galaxy AI " + context.getString(R.string.gemini_nano_status_available)
                            statusIsPositive = true
                        } else {
                            statusMessage = context.getString(R.string.gemini_nano_status_unavailable)
                            statusIsPositive = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.gemini_nano_check_status))
            }

            // Status message
            statusMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusIsPositive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (statusIsPositive) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (statusIsPositive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (statusIsPositive) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            HorizontalDivider()

            // Config parameters
            Text(
                stringResource(R.string.ai_temperature) + ": %.1f".format(config.temperature),
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = config.temperature,
                onValueChange = { onUpdate(config.copy(temperature = it)) },
                valueRange = 0f..1f,
                steps = 9
            )
            OutlinedTextField(
                value = config.topK.toString(),
                onValueChange = { onUpdate(config.copy(topK = it.toIntOrNull() ?: 40)) },
                label = { Text(stringResource(R.string.gemini_top_k)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = config.maxOutputTokens.toString(),
                onValueChange = { onUpdate(config.copy(maxOutputTokens = it.toIntOrNull() ?: 1024)) },
                label = { Text(stringResource(R.string.ai_max_tokens)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

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

@Composable
private fun LocalAnalysisInfoCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.ai_local_analysis_always_available),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                stringResource(R.string.ai_local_analysis_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LocalConfigSection(
    config: LocalAiConfig,
    onUpdate: (LocalAiConfig) -> Unit
) {
    val context = LocalContext.current
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onUpdate(config.copy(modelPath = it.toString()))
        }
    }

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
                value = config.modelPath,
                onValueChange = { onUpdate(config.copy(modelPath = it)) },
                label = { Text(stringResource(R.string.ai_model_path)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { filePicker.launch(arrayOf("*/*")) }) {
                        Icon(Icons.Default.FolderOpen, contentDescription = stringResource(R.string.ai_browse_model))
                    }
                }
            )
            OutlinedTextField(
                value = config.contextSize.toString(),
                onValueChange = { onUpdate(config.copy(contextSize = it.toIntOrNull() ?: 2048)) },
                label = { Text(stringResource(R.string.ai_context_size)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = config.maxTokens.toString(),
                onValueChange = { onUpdate(config.copy(maxTokens = it.toIntOrNull() ?: 1024)) },
                label = { Text(stringResource(R.string.ai_max_tokens)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Text(
                stringResource(R.string.ai_temperature) + ": %.1f".format(config.temperature),
                style = MaterialTheme.typography.bodySmall
            )
            Slider(
                value = config.temperature,
                onValueChange = { onUpdate(config.copy(temperature = it)) },
                valueRange = 0f..1f,
                steps = 9
            )
        }
    }
}
