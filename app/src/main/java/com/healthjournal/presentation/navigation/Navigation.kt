package com.healthjournal.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector
import com.healthjournal.R

sealed class Screen(val route: String, @StringRes val titleResId: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Dashboard)
    data object Vitals : Screen("vitals", R.string.nav_vitals, Icons.Default.MonitorHeart)
    data object Medications : Screen("medications", R.string.nav_medications, Icons.Default.Medication)
    data object Reports : Screen("reports", R.string.nav_reports, Icons.Default.Summarize)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Tune)
    data object ProfileSelection : Screen("profile_selection", R.string.profile_selection_title, Icons.Default.Groups)
    data object Onboarding : Screen("onboarding", R.string.nav_home, Icons.Default.Home)
    data object AddSymptom : Screen("add_symptom", R.string.add_symptom_title, Icons.Default.Home)
    data object EditSymptom : Screen("edit_symptom/{symptomId}", R.string.edit_symptom_title, Icons.Default.Home) {
        fun createRoute(symptomId: Long) = "edit_symptom/$symptomId"
    }
    data object AddVital : Screen("add_vital", R.string.add_vital_title, Icons.Default.Favorite)
    data object EditVital : Screen("edit_vital/{vitalId}", R.string.edit_vital_title, Icons.Default.Favorite) {
        fun createRoute(vitalId: Long) = "edit_vital/$vitalId"
    }
    data object AddMedication : Screen("add_medication", R.string.add_medication_title, Icons.Default.Medication)
    data object EditMedication : Screen("edit_medication/{medicationId}", R.string.edit_medication_title, Icons.Default.Medication) {
        fun createRoute(medicationId: Long) = "edit_medication/$medicationId"
    }
    data object LanguageSettings : Screen("language_settings", R.string.language_settings_title, Icons.Default.Settings)
    data object AiSettings : Screen("ai_settings", R.string.ai_settings_title, Icons.Default.Psychology)
    data object UserInfo : Screen("user_info", R.string.user_info_title, Icons.Default.Settings)
    data object FamilyMembers : Screen("family_members", R.string.family_members_title, Icons.Default.Settings)
    data object PredefinedDataSettings : Screen("predefined_data_settings", R.string.predefined_data_title, Icons.Default.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Vitals, Screen.Medications, Screen.Reports, Screen.Settings)
