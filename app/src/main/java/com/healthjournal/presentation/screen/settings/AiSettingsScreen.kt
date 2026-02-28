package com.healthjournal.presentation.screen.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
                    // Provider selection header
                    Text(
                        stringResource(R.string.ai_select_provider),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // On-device AI (Gemini Nano) — recommended
                    ProviderCard(
                        icon = Icons.Default.PhoneAndroid,
                        name = stringResource(R.string.ai_provider_system),
                        description = stringResource(R.string.system_ai_desc),
                        badge = stringResource(R.string.system_ai_recommended),
                        badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                        badgeTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        selected = settings.selectedProviderId == AiProviderId.GEMINI_NANO.key,
                        onClick = { viewModel.selectProvider(AiProviderId.GEMINI_NANO.key) }
                    )

                    AnimatedVisibility(visible = settings.selectedProviderId == AiProviderId.GEMINI_NANO.key) {
                        GeminiNanoConfigCard(
                            config = settings.geminiNanoConfig,
                            onUpdate = { viewModel.updateGeminiNanoConfig(it) }
                        )
                    }

                    HorizontalDivider()

                    // Claude
                    ProviderCard(
                        icon = Icons.Default.Star,
                        name = stringResource(R.string.ai_provider_claude),
                        description = stringResource(R.string.ai_api_key_required),
                        selected = settings.selectedProviderId == AiProviderId.CLAUDE.key,
                        onClick = { viewModel.selectProvider(AiProviderId.CLAUDE.key) }
                    )

                    AnimatedVisibility(visible = settings.selectedProviderId == AiProviderId.CLAUDE.key) {
                        ClaudeConfigCard(
                            config = settings.claudeConfig,
                            onUpdate = { viewModel.updateClaudeConfig(it) }
                        )
                    }

                    // OpenAI-compatible
                    ProviderCard(
                        icon = Icons.Default.Cloud,
                        name = stringResource(R.string.ai_provider_openai),
                        description = stringResource(R.string.ai_api_key_required),
                        selected = settings.selectedProviderId == AiProviderId.OPENAI_COMPATIBLE.key,
                        onClick = { viewModel.selectProvider(AiProviderId.OPENAI_COMPATIBLE.key) }
                    )

                    AnimatedVisibility(visible = settings.selectedProviderId == AiProviderId.OPENAI_COMPATIBLE.key) {
                        OpenAiConfigCard(
                            config = settings.openAiConfig,
                            onUpdate = { viewModel.updateOpenAiConfig(it) }
                        )
                    }

                    HorizontalDivider()

                    // Local model
                    ProviderCard(
                        icon = Icons.Default.Folder,
                        name = stringResource(R.string.ai_provider_local),
                        description = stringResource(R.string.ai_local_model_desc),
                        selected = settings.selectedProviderId == AiProviderId.LOCAL.key,
                        onClick = { viewModel.selectProvider(AiProviderId.LOCAL.key) }
                    )

                    AnimatedVisibility(visible = settings.selectedProviderId == AiProviderId.LOCAL.key) {
                        LocalConfigCard(
                            config = settings.localAiConfig,
                            onUpdate = { viewModel.updateLocalConfig(it) }
                        )
                    }

                    HorizontalDivider()

                    // Privacy redaction
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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

@Composable
private fun ProviderCard(
    icon: ImageVector,
    name: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    badge: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    badgeTextColor: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    Card(
        onClick = onClick,
        colors = if (selected) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else CardDefaults.elevatedCardColors(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (badge != null) {
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = badgeColor
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun GeminiNanoConfigCard(
    config: GeminiNanoConfig,
    onUpdate: (GeminiNanoConfig) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.ai_provider_gemini_nano),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                stringResource(R.string.gemini_nano_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun ClaudeConfigCard(
    config: ClaudeConfig,
    onUpdate: (ClaudeConfig) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
private fun OpenAiConfigCard(
    config: OpenAiConfig,
    onUpdate: (OpenAiConfig) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
private fun LocalConfigCard(
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

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
