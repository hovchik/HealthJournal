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
import androidx.compose.material.icons.filled.Star
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
    data object DiseaseDetail : Screen("disease_detail/{diseaseId}", R.string.home_title, Icons.Default.Home) {
        fun createRoute(diseaseId: Long) = "disease_detail/$diseaseId"
    }
    data object FamilyDiseases : Screen("family_diseases", R.string.family_diseases_title, Icons.Default.Groups)
    data object AddSymptomToDisease : Screen("add_symptom_disease/{diseaseId}", R.string.add_symptom_title, Icons.Default.Home) {
        fun createRoute(diseaseId: Long) = "add_symptom_disease/$diseaseId"
    }
    data object AddVitalToDisease : Screen("add_vital_disease/{diseaseId}", R.string.add_vital_title, Icons.Default.Favorite) {
        fun createRoute(diseaseId: Long) = "add_vital_disease/$diseaseId"
    }
    data object AddMedicationToDisease : Screen("add_medication_disease/{diseaseId}", R.string.add_medication_title, Icons.Default.Medication) {
        fun createRoute(diseaseId: Long) = "add_medication_disease/$diseaseId"
    }
    data object LanguageSettings : Screen("language_settings", R.string.language_settings_title, Icons.Default.Settings)
    data object AiSettings : Screen("ai_settings", R.string.ai_settings_title, Icons.Default.Psychology)
    data object UserInfo : Screen("user_info", R.string.user_info_title, Icons.Default.Settings)
    data object FamilyMembers : Screen("family_members", R.string.family_members_title, Icons.Default.Settings)
    data object PredefinedDataSettings : Screen("predefined_data_settings", R.string.predefined_data_title, Icons.Default.Settings)
    data object DeletedDiseases : Screen("deleted_diseases", R.string.deleted_diseases, Icons.Default.Settings)
    data object DiseaseAiAnalysis : Screen("disease_ai_analysis/{diseaseId}", R.string.disease_ai_analysis_title, Icons.Default.Psychology) {
        fun createRoute(diseaseId: Long) = "disease_ai_analysis/$diseaseId"
    }
    data object Subscription : Screen("subscription", R.string.subscription_title, Icons.Default.Star)
}

val bottomNavItems = listOf(Screen.Home, Screen.Reports, Screen.Settings)
