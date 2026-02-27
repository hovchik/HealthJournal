package com.healthjournal.presentation.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
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

    // Reorder providers: Gemini Nano first (system AI), then others
    val orderedProviders = remember(viewModel.providers) {
        val gemini = viewModel.providers.filter { it.id == AiProviderId.GEMINI_NANO }
        val rest = viewModel.providers.filter { it.id != AiProviderId.GEMINI_NANO }
        gemini + rest
    }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.ai_enabled),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = settings.enabled,
                        onCheckedChange = { viewModel.toggleEnabled(it) }
                    )
                }
            }

            if (settings.enabled) {
                // Provider selection
                Text(
                    stringResource(R.string.ai_select_provider),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                orderedProviders.forEach { provider ->
                    val selected = provider.id.key == settings.selectedProviderId
                    val isSystemAi = provider.id == AiProviderId.GEMINI_NANO

                    Card(
                        onClick = { viewModel.selectProvider(provider.id.key) },
                        colors = if (selected) CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else CardDefaults.cardColors(),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (selected) 4.dp else 1.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isSystemAi) {
                                        Icon(
                                            Icons.Default.PhoneAndroid,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
                                                   else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        if (isSystemAi) stringResource(R.string.ai_provider_system)
                                        else stringResource(provider.displayNameResId),
                                        fontWeight = if (isSystemAi) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                    if (isSystemAi) {
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
                                }
                                RadioButton(
                                    selected = selected,
                                    onClick = { viewModel.selectProvider(provider.id.key) }
                                )
                            }
                            if (isSystemAi) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    stringResource(R.string.system_ai_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Privacy redaction toggle
                HorizontalDivider()
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.ai_privacy_redact),
                                style = MaterialTheme.typography.titleSmall
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

                // Provider-specific config
                HorizontalDivider()
                when (AiProviderId.fromKey(settings.selectedProviderId)) {
                    AiProviderId.CLAUDE -> ClaudeConfigSection(
                        config = settings.claudeConfig,
                        onUpdate = { viewModel.updateClaudeConfig(it) }
                    )
                    AiProviderId.OPENAI_COMPATIBLE -> OpenAiConfigSection(
                        config = settings.openAiConfig,
                        onUpdate = { viewModel.updateOpenAiConfig(it) }
                    )
                    AiProviderId.GEMINI_NANO -> GeminiNanoConfigSection(
                        config = settings.geminiNanoConfig,
                        onUpdate = { viewModel.updateGeminiNanoConfig(it) }
                    )
                    AiProviderId.LOCAL -> LocalConfigSection(
                        config = settings.localAiConfig,
                        onUpdate = { viewModel.updateLocalConfig(it) }
                    )
                }

                // Validate button
                Button(
                    onClick = { viewModel.validateCurrentProvider() },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(stringResource(R.string.ai_validate_config))
                }

                validationMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            msg,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClaudeConfigSection(
    config: ClaudeConfig,
    onUpdate: (ClaudeConfig) -> Unit
) {
    Text(
        stringResource(R.string.ai_provider_claude),
        style = MaterialTheme.typography.titleMedium
    )
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

@Composable
private fun OpenAiConfigSection(
    config: OpenAiConfig,
    onUpdate: (OpenAiConfig) -> Unit
) {
    Text(
        stringResource(R.string.ai_provider_openai),
        style = MaterialTheme.typography.titleMedium
    )
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

@Composable
private fun GeminiNanoConfigSection(
    config: GeminiNanoConfig,
    onUpdate: (GeminiNanoConfig) -> Unit
) {
    Text(
        stringResource(R.string.ai_provider_gemini_nano),
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        stringResource(R.string.gemini_nano_desc),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = config.temperature.toString(),
        onValueChange = { onUpdate(config.copy(temperature = it.toFloatOrNull() ?: 0.7f)) },
        label = { Text(stringResource(R.string.ai_temperature)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
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

@Composable
private fun LocalConfigSection(
    config: LocalAiConfig,
    onUpdate: (LocalAiConfig) -> Unit
) {
    Text(
        stringResource(R.string.ai_provider_local),
        style = MaterialTheme.typography.titleMedium
    )
    OutlinedTextField(
        value = config.modelPath,
        onValueChange = { onUpdate(config.copy(modelPath = it)) },
        label = { Text(stringResource(R.string.ai_model_path)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
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
    OutlinedTextField(
        value = config.temperature.toString(),
        onValueChange = { onUpdate(config.copy(temperature = it.toFloatOrNull() ?: 0.7f)) },
        label = { Text(stringResource(R.string.ai_temperature)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true
    )
}
