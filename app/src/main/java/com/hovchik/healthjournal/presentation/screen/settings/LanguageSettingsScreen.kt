package com.hovchik.healthjournal.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hovchik.healthjournal.R

private data class LanguageOption(
    val code: String,
    val labelResId: Int
)

private val languageOptions = listOf(
    LanguageOption("SYSTEM", R.string.language_system_default),
    LanguageOption("ru", R.string.language_russian),
    LanguageOption("en", R.string.language_english),
    LanguageOption("zh-CN", R.string.language_chinese),
    LanguageOption("es", R.string.language_spanish),
    LanguageOption("hy", R.string.language_armenian)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    onBack: () -> Unit,
    viewModel: LanguageSettingsViewModel = viewModel()
) {
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_settings_title)) },
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
        ) {
            languageOptions.forEach { option ->
                ListItem(
                    headlineContent = { Text(stringResource(option.labelResId)) },
                    trailingContent = {
                        if (currentLanguage == option.code) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        viewModel.setLanguage(option.code)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
