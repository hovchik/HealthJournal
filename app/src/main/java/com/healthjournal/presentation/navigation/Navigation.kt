package com.healthjournal.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.healthjournal.R

sealed class Screen(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Dashboard)
    data object Vitals : Screen("vitals", R.string.nav_vitals, Icons.Default.MonitorHeart)
    data object Medications : Screen("medications", R.string.nav_medications, Icons.Default.Medication)
    data object AiReport : Screen("ai_report", R.string.nav_ai_report, Icons.Default.AutoAwesome)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Tune)
    data object Onboarding : Screen("onboarding", R.string.nav_home, Icons.Default.Home)
    data object AddSymptom : Screen("add_symptom", R.string.add_symptom_title, Icons.Default.Home)
    data object AddVital : Screen("add_vital", R.string.add_vital_title, Icons.Default.Favorite)
    data object AddMedication : Screen("add_medication", R.string.add_medication_title, Icons.Default.Medication)
    data object LanguageSettings : Screen("language_settings", R.string.language_settings_title, Icons.Default.Settings)
    data object AiSettings : Screen("ai_settings", R.string.ai_settings_title, Icons.Default.Psychology)
    data object UserInfo : Screen("user_info", R.string.user_info_title, Icons.Default.Settings)
    data object FamilyMembers : Screen("family_members", R.string.family_members_title, Icons.Default.Settings)
    data object PredefinedDataSettings : Screen("predefined_data_settings", R.string.predefined_data_title, Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Vitals, Screen.Medications, Screen.AiReport, Screen.Settings)
