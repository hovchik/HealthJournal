package com.healthjournal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.healthjournal.presentation.screen.ai.AiReportScreen
import com.healthjournal.presentation.screen.home.AddSymptomScreen
import com.healthjournal.presentation.screen.home.AddVitalScreen
import com.healthjournal.presentation.screen.home.HomeScreen
import com.healthjournal.presentation.screen.medications.AddMedicationScreen
import com.healthjournal.presentation.screen.medications.MedicationsScreen
import com.healthjournal.presentation.screen.onboarding.OnboardingScreen
import com.healthjournal.presentation.screen.settings.LanguageSettingsScreen
import com.healthjournal.presentation.screen.settings.SettingsScreen
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
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
            VitalsScreen()
        }
        composable(Screen.Medications.route) {
            MedicationsScreen(
                onAddMedication = { navController.navigate(Screen.AddMedication.route) }
            )
        }
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AiReport.route) {
            AiReportScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLanguageSettings = { navController.navigate(Screen.LanguageSettings.route) }
            )
        }
        composable(Screen.LanguageSettings.route) {
            LanguageSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
