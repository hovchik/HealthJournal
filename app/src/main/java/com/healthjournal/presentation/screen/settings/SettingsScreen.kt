package com.healthjournal.presentation.screen.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings as AndroidSettings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    onDeletedDiseases: () -> Unit,
    onSubscription: () -> Unit,
    onReminders: () -> Unit = {},
    onDashboard: () -> Unit = {},
    onAppointments: () -> Unit = {},
    onAiChat: () -> Unit = {},
    onSecurity: () -> Unit = {},
    onAchievements: () -> Unit = {},
    onHealthConnect: () -> Unit = {},
    onFamilyDashboard: () -> Unit = {},
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    val isAnyLoading = uiState.isExporting || uiState.isImporting || uiState.isSharing

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.nav_settings),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subscription section
            SettingsSection(
                title = stringResource(R.string.settings_section_subscription),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                SettingsGroupCard {
                    SettingsListItem(
                        icon = Icons.Default.Star,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.subscription_title),
                        subtitle = stringResource(R.string.subscription_desc),
                        onClick = onSubscription
                    )
                }
            }

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
                        icon = Icons.Default.RestoreFromTrash,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.deleted_diseases),
                        subtitle = stringResource(R.string.deleted_diseases_desc),
                        onClick = onDeletedDiseases
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Export/Import
                    SettingsListItem(
                        icon = Icons.Default.FileDownload,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.export_import_title),
                        subtitle = stringResource(R.string.export_import_desc),
                        onClick = {}
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { exportLauncher.launch("health_journal_backup.json") },
                            enabled = !isAnyLoading,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (uiState.isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_export))
                        }
                        FilledTonalButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            enabled = !isAnyLoading,
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
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
                    // Share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { settingsViewModel.shareData(context) },
                            enabled = !isAnyLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            if (uiState.isSharing) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.backup_share))
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
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
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
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

                    // Theme selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                stringResource(R.string.theme_settings_title),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val themeOptions = listOf(
                                "SYSTEM" to stringResource(R.string.theme_system),
                                "LIGHT" to stringResource(R.string.theme_light),
                                "DARK" to stringResource(R.string.theme_dark),
                                "AMOLED" to stringResource(R.string.theme_amoled)
                            )
                            themeOptions.forEachIndexed { index, (mode, label) ->
                                SegmentedButton(
                                    selected = settings.themeMode == mode,
                                    onClick = { settingsViewModel.setThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = themeOptions.size
                                    ),
                                    icon = {}
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

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

            // Features section
            SettingsSection(
                title = stringResource(R.string.settings_section_features),
                color = MaterialTheme.colorScheme.primary
            ) {
                SettingsGroupCard {
                    SettingsListItem(
                        icon = Icons.Default.Notifications,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = stringResource(R.string.reminders_title),
                        subtitle = stringResource(R.string.reminders_desc),
                        onClick = onReminders
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Timeline,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.dashboard_title),
                        subtitle = stringResource(R.string.dashboard_desc),
                        onClick = onDashboard
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Watch,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        title = stringResource(R.string.health_connect_title),
                        subtitle = stringResource(R.string.health_connect_desc),
                        onClick = onHealthConnect
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.FamilyRestroom,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = stringResource(R.string.family_dashboard_title),
                        subtitle = stringResource(R.string.family_dashboard_desc),
                        onClick = onFamilyDashboard
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsListItem(
                        icon = Icons.Default.Security,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = stringResource(R.string.security_title),
                        subtitle = stringResource(R.string.security_desc),
                        onClick = onSecurity
                    )
                }
            }

            // Permissions section
            PermissionsSection()

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PermissionsSection() {
    val context = LocalContext.current

    var notificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationGranted = granted
    }

    val app = context.applicationContext as com.healthjournal.HealthJournalApp
    val localModelManager = app.container.localModelManager
    var aiAvailable by remember { mutableStateOf<Boolean?>(null) }
    var aiStatusLabel by remember { mutableStateOf("") }

    fun checkAiAvailability() {
        val activeModel = localModelManager.getActiveModelSync()
        aiAvailable = activeModel != null
        aiStatusLabel = activeModel?.displayName ?: ""
    }

    SettingsSection(
        title = stringResource(R.string.settings_section_permissions),
        color = MaterialTheme.colorScheme.error
    ) {
        SettingsGroupCard {
            if (Build.VERSION.SDK_INT >= 33) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.perm_notifications),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.perm_notifications_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (notificationGranted) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        FilledTonalButton(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(stringResource(R.string.perm_grant))
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.perm_ai_status),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    when (aiAvailable) {
                        true -> Text(
                            "${stringResource(R.string.perm_ai_available)} ($aiStatusLabel)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        false -> Text(
                            stringResource(R.string.perm_ai_no_model),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        null -> Text(
                            stringResource(R.string.perm_ai_status),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (aiAvailable == true) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        onClick = { checkAiAvailability() },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(stringResource(R.string.perm_ai_check))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Surface(
                onClick = {
                    val intent = Intent(AndroidSettings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
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
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.perm_open_settings),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            stringResource(R.string.perm_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        content()
    }
}

@Composable
private fun SettingsGroupCard(
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
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
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp))
        }
    }
}
