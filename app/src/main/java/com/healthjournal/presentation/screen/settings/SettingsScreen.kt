package com.healthjournal.presentation.screen.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthjournal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageSettings: () -> Unit,
    onAiSettings: () -> Unit,
    onUserInfo: () -> Unit,
    onFamilyMembers: () -> Unit,
    onPredefinedData: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val exportSuccessMsg = stringResource(R.string.export_success)
    val importSuccessMsg = stringResource(R.string.import_success)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.exportData(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { settingsViewModel.importData(it) }
    }

    // Cloud export: SAF with initial URI hint for cloud providers
    val cloudExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { settingsViewModel.exportData(it) }
    }

    val cloudImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { settingsViewModel.importData(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.nav_settings),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Profile section
            SettingsSection(
                title = stringResource(R.string.settings_section_profile),
                color = MaterialTheme.colorScheme.primary
            ) {
                SettingsGroupCard {
                    SettingsListItem(
                        icon = Icons.Default.Person,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.user_info_title),
                        subtitle = stringResource(R.string.user_info_desc),
                        onClick = onUserInfo
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Group,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.family_members_title),
                        subtitle = stringResource(R.string.family_members_desc),
                        onClick = onFamilyMembers
                    )
                }
            }

            // Data section
            SettingsSection(
                title = stringResource(R.string.settings_section_data),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                SettingsGroupCard {
                    SettingsListItem(
                        icon = Icons.Default.Checklist,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.predefined_data_title),
                        subtitle = stringResource(R.string.predefined_data_desc),
                        onClick = onPredefinedData
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.FileDownload,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.export_import_title),
                        subtitle = stringResource(R.string.export_import_desc),
                        onClick = {}
                    )
                    // Export/Import buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { exportLauncher.launch("health_journal_backup.json") },
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_export))
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isImporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_import))
                        }
                    }
                    // Cloud backup section
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Cloud,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.backup_cloud_title),
                        subtitle = stringResource(R.string.backup_cloud_desc),
                        onClick = {}
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { cloudExportLauncher.launch("health_journal_backup.json") },
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_export))
                        }
                        OutlinedButton(
                            onClick = { cloudImportLauncher.launch(arrayOf("application/json")) },
                            enabled = !uiState.isExporting && !uiState.isImporting,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_import))
                        }
                    }
                }

                // Status message
                uiState.message?.let { message ->
                    val displayMessage = when (message) {
                        "export_success" -> exportSuccessMsg
                        "import_success" -> importSuccessMsg
                        else -> message
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isError) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    if (uiState.isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (uiState.isError) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    displayMessage,
                                    color = if (uiState.isError) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(onClick = { settingsViewModel.clearMessage() }) {
                                Text(stringResource(R.string.close))
                            }
                        }
                    }
                }
            }

            // Preferences section
            SettingsSection(
                title = stringResource(R.string.settings_section_preferences),
                color = MaterialTheme.colorScheme.secondary
            ) {
                SettingsGroupCard {
                    SettingsListItem(
                        icon = Icons.Default.Language,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = stringResource(R.string.language_settings_title),
                        subtitle = stringResource(R.string.language_select_prompt),
                        onClick = onLanguageSettings
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Psychology,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.ai_settings_title),
                        subtitle = stringResource(R.string.ai_settings_desc),
                        onClick = onAiSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    color: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsListItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(18.dp))
        }
    }
}
