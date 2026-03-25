package com.healthjournal.presentation.screen.gamification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Achievement
import com.healthjournal.domain.model.GamificationStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AchievementsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container
    private val gamificationManager = container.gamificationManager

    val stats = gamificationManager.stats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GamificationStats())

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    init {
        viewModelScope.launch {
            _achievements.value = gamificationManager.getAllAchievements()

            // Check data-based achievements
            val symptoms = container.symptomRepository.getAllSymptoms().first()
            val vitals = container.vitalSignRepository.getAllVitalSigns().first()
            val meds = container.medicationRepository.getAllMedications().first()
            val reports = container.aiReportRepository.getAllReports().first()

            gamificationManager.checkDataAchievements(
                symptomCount = symptoms.size,
                vitalCount = vitals.size,
                medicationCount = meds.size,
                reportCount = reports.size
            )
            _achievements.value = gamificationManager.getAllAchievements()
        }
    }
}
