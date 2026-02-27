package com.healthjournal.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.healthjournal.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLanguageSettings: () -> Unit,
    onAiSettings: () -> Unit,
    onUserInfo: () -> Unit,
    onFamilyMembers: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.user_info_title)) },
                supportingContent = { Text(stringResource(R.string.user_info_desc)) },
                leadingContent = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onUserInfo)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.family_members_title)) },
                supportingContent = { Text(stringResource(R.string.family_members_desc)) },
                leadingContent = {
                    Icon(Icons.Default.Group, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onFamilyMembers)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.language_settings_title)) },
                supportingContent = { Text(stringResource(R.string.language_select_prompt)) },
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onLanguageSettings)
            )
            HorizontalDivider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.ai_settings_title)) },
                supportingContent = { Text(stringResource(R.string.ai_settings_desc)) },
                leadingContent = {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                },
                trailingContent = {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                },
                modifier = Modifier.clickable(onClick = onAiSettings)
            )
            HorizontalDivider()
        }
    }
}
