package com.healthjournal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.healthjournal.presentation.screen.ai.ReportsScreen
import com.healthjournal.presentation.screen.home.AddSymptomScreen
import com.healthjournal.presentation.screen.home.AddVitalScreen
import com.healthjournal.presentation.screen.home.HomeScreen
import com.healthjournal.presentation.screen.home.ProfileSelectionScreen
import com.healthjournal.presentation.screen.medications.AddMedicationScreen
import com.healthjournal.presentation.screen.medications.MedicationsScreen
import com.healthjournal.presentation.screen.onboarding.OnboardingScreen
import com.healthjournal.presentation.screen.settings.AiSettingsScreen
import com.healthjournal.presentation.screen.settings.FamilyMembersScreen
import com.healthjournal.presentation.screen.settings.LanguageSettingsScreen
import com.healthjournal.presentation.screen.settings.PredefinedDataSettingsScreen
import com.healthjournal.presentation.screen.settings.SettingsScreen
import com.healthjournal.presentation.screen.settings.UserInfoScreen
import com.healthjournal.presentation.screen.vitals.VitalsScreen

@Composable
fun HealthNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.ProfileSelection.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.ProfileSelection.route) {
            ProfileSelectionScreen(
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.ProfileSelection.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onAddSymptom = { navController.navigate(Screen.AddSymptom.route) },
                onAddVital = { navController.navigate(Screen.AddVital.route) }
            )
        }
        composable(Screen.AddSymptom.route) {
            AddSymptomScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AddVital.route) {
            AddVitalScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Vitals.route) {
            VitalsScreen(
                onAddVital = { navController.navigate(Screen.AddVital.route) }
            )
        }
        composable(Screen.Medications.route) {
            MedicationsScreen(
                onAddMedication = { navController.navigate(Screen.AddMedication.route) }
            )
        }
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Reports.route) {
            ReportsScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLanguageSettings = { navController.navigate(Screen.LanguageSettings.route) },
                onAiSettings = { navController.navigate(Screen.AiSettings.route) },
                onUserInfo = { navController.navigate(Screen.UserInfo.route) },
                onFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) },
                onPredefinedData = { navController.navigate(Screen.PredefinedDataSettings.route) }
            )
        }
        composable(Screen.LanguageSettings.route) {
            LanguageSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AiSettings.route) {
            AiSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.UserInfo.route) {
            UserInfoScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.PredefinedDataSettings.route) {
            PredefinedDataSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
