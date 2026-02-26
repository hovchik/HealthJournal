package com.healthjournal.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Главная", Icons.Default.Home)
    data object Vitals : Screen("vitals", "Показатели", Icons.Default.Favorite)
    data object Medications : Screen("medications", "Лекарства", Icons.Default.Medication)
    data object AiReport : Screen("ai_report", "AI Отчёт", Icons.Default.Psychology)
    data object Onboarding : Screen("onboarding", "Онбординг", Icons.Default.Home)
    data object AddSymptom : Screen("add_symptom", "Добавить симптом", Icons.Default.Home)
    data object AddVital : Screen("add_vital", "Добавить показатель", Icons.Default.Favorite)
    data object AddMedication : Screen("add_medication", "Добавить лекарство", Icons.Default.Medication)
}

val bottomNavItems = listOf(Screen.Home, Screen.Vitals, Screen.Medications, Screen.AiReport)
