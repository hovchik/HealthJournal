package com.healthjournal.presentation.screen.settings

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
import com.healthjournal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageSettings: () -> Unit,
    onAiSettings: () -> Unit,
    onUserInfo: () -> Unit,
    onFamilyMembers: () -> Unit,
    onPredefinedData: () -> Unit
) {
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
