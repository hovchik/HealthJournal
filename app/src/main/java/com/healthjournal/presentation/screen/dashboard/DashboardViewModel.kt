package com.healthjournal.presentation.screen.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.LocalDateTime

data class DashboardUiState(
    val symptoms: List<Symptom> = emptyList(),
    val vitals: List<VitalSign> = emptyList(),
    val vitalsByType: Map<VitalType, List<VitalSign>> = emptyMap(),
    val symptomsByDay: Map<LocalDate, Int> = emptyMap(),
    val selectedPeriodDays: Int = 7,
    val correlations: List<CorrelationItem> = emptyList()
)

data class CorrelationItem(
    val label: String,
    val description: String,
    val strength: Float
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val _periodDays = MutableStateFlow(7)

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val uiState = combine(
        container.getAllSymptoms(),
        container.getAllVitalSigns(),
        activeProfileFlow,
        _periodDays
    ) { symptoms, vitals, profileId, days ->
        val cutoff = LocalDateTime.now().minusDays(days.toLong())
        val filteredSymptoms = symptoms.filter { it.profileId == profileId && it.recordedAt.isAfter(cutoff) }
        val filteredVitals = vitals.filter { it.profileId == profileId && it.recordedAt.isAfter(cutoff) }

        val vitalsByType = filteredVitals.groupBy { it.type }
        val symptomsByDay = filteredSymptoms.groupBy { it.recordedAt.toLocalDate() }
            .mapValues { it.value.size }

        val correlations = findCorrelations(filteredSymptoms, filteredVitals)

        DashboardUiState(
            symptoms = filteredSymptoms,
            vitals = filteredVitals,
            vitalsByType = vitalsByType,
            symptomsByDay = symptomsByDay,
            selectedPeriodDays = days,
            correlations = correlations
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun setPeriod(days: Int) {
        _periodDays.value = days
    }

    private fun findCorrelations(symptoms: List<Symptom>, vitals: List<VitalSign>): List<CorrelationItem> {
        val results = mutableListOf<CorrelationItem>()

        // Check if high-intensity symptoms correlate with vital anomalies
        val highIntensityDays = symptoms.filter { it.intensity >= 7 }.map { it.recordedAt.toLocalDate() }.toSet()
        val normalDays = symptoms.filter { it.intensity < 4 }.map { it.recordedAt.toLocalDate() }.toSet()

        for ((type, typeVitals) in vitals.groupBy { it.type }) {
            val highDayValues = typeVitals.filter { it.recordedAt.toLocalDate() in highIntensityDays }.map { it.value }
            val normalDayValues = typeVitals.filter { it.recordedAt.toLocalDate() in normalDays }.map { it.value }

            if (highDayValues.size >= 2 && normalDayValues.size >= 2) {
                val highAvg = highDayValues.average()
                val normalAvg = normalDayValues.average()
                val diff = kotlin.math.abs(highAvg - normalAvg)
                val maxRange = typeVitals.maxOf { it.value } - typeVitals.minOf { it.value }

                if (maxRange > 0) {
                    val strength = (diff / maxRange).toFloat().coerceIn(0f, 1f)
                    if (strength > 0.1f) {
                        results.add(
                            CorrelationItem(
                                label = "${type.displayName} ↔ Symptoms",
                                description = "Avg ${type.displayName}: %.1f on high-symptom days vs %.1f on low-symptom days".format(highAvg, normalAvg),
                                strength = strength
                            )
                        )
                    }
                }
            }
        }

        return results.sortedByDescending { it.strength }
    }
}
