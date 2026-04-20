package com.hovchik.healthjournal.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.hovchik.healthjournal.presentation.screen.ai.AiChatScreen
import com.hovchik.healthjournal.presentation.screen.ai.ReportsScreen
import com.hovchik.healthjournal.presentation.screen.appointments.AppointmentsScreen
import com.hovchik.healthjournal.presentation.screen.dashboard.DashboardScreen
import com.hovchik.healthjournal.presentation.screen.disease.DiseaseDetailScreen
import com.hovchik.healthjournal.presentation.screen.disease.DiseaseListScreen
import com.hovchik.healthjournal.presentation.screen.disease.FamilyDiseaseComparisonScreen
import com.hovchik.healthjournal.presentation.screen.family.FamilyDashboardScreen
import com.hovchik.healthjournal.presentation.screen.gamification.AchievementsScreen
import com.hovchik.healthjournal.presentation.screen.home.AddSymptomScreen
import com.hovchik.healthjournal.presentation.screen.home.AddVitalScreen
import com.hovchik.healthjournal.presentation.screen.home.HomeScreen
import com.hovchik.healthjournal.presentation.screen.home.ProfileSelectionScreen
import com.hovchik.healthjournal.presentation.screen.legal.LegalDocumentScreen
import com.hovchik.healthjournal.presentation.screen.medications.AddMedicationScreen
import com.hovchik.healthjournal.presentation.screen.medications.MedicationsScreen
import com.hovchik.healthjournal.presentation.screen.onboarding.OnboardingScreen
import com.hovchik.healthjournal.presentation.screen.reminders.RemindersScreen
import com.hovchik.healthjournal.presentation.screen.security.SecuritySettingsScreen
import com.hovchik.healthjournal.presentation.screen.settings.AiSettingsScreen
import com.hovchik.healthjournal.presentation.screen.settings.FamilyMembersScreen
import com.hovchik.healthjournal.presentation.screen.settings.HealthConnectScreen
import com.hovchik.healthjournal.presentation.screen.settings.LanguageSettingsScreen
import com.hovchik.healthjournal.presentation.screen.settings.PredefinedDataSettingsScreen
import com.hovchik.healthjournal.presentation.screen.disease.DiseaseAiAnalysisScreen
import com.hovchik.healthjournal.presentation.screen.settings.DeletedDiseasesScreen
import com.hovchik.healthjournal.presentation.screen.settings.SettingsScreen
import com.hovchik.healthjournal.presentation.screen.settings.UserInfoScreen
import com.hovchik.healthjournal.presentation.screen.subscription.SubscriptionScreen
import com.hovchik.healthjournal.presentation.screen.vitals.VitalsScreen

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
                },
                onOpenLegalDocument = { docKey ->
                    navController.navigate(Screen.LegalDocument.createRoute(docKey))
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
            DiseaseListScreen(
                onDiseaseClick = { id -> navController.navigate(Screen.DiseaseDetail.createRoute(id)) },
                onAddSymptom = { navController.navigate(Screen.AddSymptom.route) },
                onAddVital = { navController.navigate(Screen.AddVital.route) },
                onAiChat = { navController.navigate(Screen.AiChat.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.AddSymptom.route) {
            AddSymptomScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.EditSymptom.route,
            arguments = listOf(navArgument("symptomId") { type = NavType.LongType })
        ) { backStackEntry ->
            val symptomId = backStackEntry.arguments?.getLong("symptomId") ?: -1L
            AddSymptomScreen(onBack = { navController.popBackStack() }, symptomId = symptomId)
        }
        composable(Screen.AddVital.route) {
            AddVitalScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.EditVital.route,
            arguments = listOf(navArgument("vitalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val vitalId = backStackEntry.arguments?.getLong("vitalId") ?: -1L
            AddVitalScreen(onBack = { navController.popBackStack() }, vitalId = vitalId)
        }
        composable(Screen.Vitals.route) {
            VitalsScreen(
                onAddVital = { navController.navigate(Screen.AddVital.route) },
                onEditVital = { id -> navController.navigate(Screen.EditVital.createRoute(id)) }
            )
        }
        composable(Screen.Medications.route) {
            MedicationsScreen(
                onAddMedication = { navController.navigate(Screen.AddMedication.route) },
                onEditMedication = { id -> navController.navigate(Screen.EditMedication.createRoute(id)) }
            )
        }
        composable(Screen.AddMedication.route) {
            AddMedicationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.EditMedication.route,
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: -1L
            AddMedicationScreen(onBack = { navController.popBackStack() }, medicationId = medicationId)
        }
        composable(
            Screen.DiseaseDetail.route,
            arguments = listOf(navArgument("diseaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getLong("diseaseId") ?: return@composable
            DiseaseDetailScreen(
                diseaseId = diseaseId,
                onBack = { navController.popBackStack() },
                onAddSymptom = { id -> navController.navigate(Screen.AddSymptomToDisease.createRoute(id)) },
                onAddVital = { id -> navController.navigate(Screen.AddVitalToDisease.createRoute(id)) },
                onAddMedication = { id -> navController.navigate(Screen.AddMedicationToDisease.createRoute(id)) },
                onEditSymptom = { id -> navController.navigate(Screen.EditSymptom.createRoute(id)) },
                onEditVital = { id -> navController.navigate(Screen.EditVital.createRoute(id)) },
                onEditMedication = { id -> navController.navigate(Screen.EditMedication.createRoute(id)) },
                onAiAnalysis = { id -> navController.navigate(Screen.DiseaseAiAnalysis.createRoute(id)) }
            )
        }
        composable(
            Screen.DiseaseAiAnalysis.route,
            arguments = listOf(navArgument("diseaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getLong("diseaseId") ?: return@composable
            DiseaseAiAnalysisScreen(
                diseaseId = diseaseId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FamilyDiseases.route) {
            FamilyDiseaseComparisonScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.AddSymptomToDisease.route,
            arguments = listOf(navArgument("diseaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getLong("diseaseId") ?: 0L
            AddSymptomScreen(onBack = { navController.popBackStack() }, diseaseId = diseaseId)
        }
        composable(
            Screen.AddVitalToDisease.route,
            arguments = listOf(navArgument("diseaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getLong("diseaseId") ?: 0L
            AddVitalScreen(onBack = { navController.popBackStack() }, diseaseId = diseaseId)
        }
        composable(
            Screen.AddMedicationToDisease.route,
            arguments = listOf(navArgument("diseaseId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diseaseId = backStackEntry.arguments?.getLong("diseaseId") ?: 0L
            AddMedicationScreen(onBack = { navController.popBackStack() }, diseaseId = diseaseId)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(
                onAiChat = { navController.navigate(Screen.AiChat.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLanguageSettings = { navController.navigate(Screen.LanguageSettings.route) },
                onAiSettings = { navController.navigate(Screen.AiSettings.route) },
                onUserInfo = { navController.navigate(Screen.UserInfo.route) },
                onFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) },
                onPredefinedData = { navController.navigate(Screen.PredefinedDataSettings.route) },
                onDeletedDiseases = { navController.navigate(Screen.DeletedDiseases.route) },
                onSubscription = { navController.navigate(Screen.Subscription.route) },
                onReminders = { navController.navigate(Screen.Reminders.route) },
                onDashboard = { navController.navigate(Screen.Dashboard.route) },
                onAppointments = { navController.navigate(Screen.Appointments.route) },
                onAiChat = { navController.navigate(Screen.AiChat.route) },
                onSecurity = { navController.navigate(Screen.SecuritySettings.route) },
                onAchievements = { navController.navigate(Screen.Achievements.route) },
                onHealthConnect = { navController.navigate(Screen.HealthConnect.route) },
                onFamilyDashboard = { navController.navigate(Screen.FamilyDashboard.route) },
                onLegalDocument = { docKey ->
                    navController.navigate(Screen.LegalDocument.createRoute(docKey))
                }
            )
        }
        composable(Screen.Subscription.route) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
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
        composable(Screen.DeletedDiseases.route) {
            DeletedDiseasesScreen(onBack = { navController.popBackStack() })
        }

        // New screens
        composable(Screen.Reminders.route) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBack = { navController.popBackStack() },
                isTopLevel = navController.previousBackStackEntry?.destination?.route == null ||
                    navController.previousBackStackEntry?.destination?.route in bottomNavItems.map { it.route },
                onAddSymptom = { navController.navigate(Screen.AddSymptom.route) },
                onAddVital = { navController.navigate(Screen.AddVital.route) },
                onAddMedication = { navController.navigate(Screen.AddMedication.route) },
                onAiChat = { navController.navigate(Screen.AiChat.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.Appointments.route) {
            AppointmentsScreen(
                onBack = { navController.popBackStack() },
                isTopLevel = navController.previousBackStackEntry?.destination?.route == null ||
                    navController.previousBackStackEntry?.destination?.route in bottomNavItems.map { it.route },
                onAiChat = { navController.navigate(Screen.AiChat.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.AiChat.route) {
            AiChatScreen(
                onBack = { navController.popBackStack() },
                isTopLevel = navController.previousBackStackEntry?.destination?.route == null ||
                    navController.previousBackStackEntry?.destination?.route in bottomNavItems.map { it.route }
            )
        }
        composable(Screen.SecuritySettings.route) {
            SecuritySettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onBack = { navController.popBackStack() },
                isTopLevel = navController.previousBackStackEntry?.destination?.route == null ||
                    navController.previousBackStackEntry?.destination?.route in bottomNavItems.map { it.route },
                onAiChat = { navController.navigate(Screen.AiChat.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.HealthConnect.route) {
            HealthConnectScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.FamilyDashboard.route) {
            FamilyDashboardScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Screen.LegalDocument.route,
            arguments = listOf(navArgument("docKey") { type = NavType.StringType })
        ) { backStackEntry ->
            val docKey = backStackEntry.arguments?.getString("docKey").orEmpty()
            LegalDocumentScreen(docKey = docKey, onBack = { navController.popBackStack() })
        }
    }
}
