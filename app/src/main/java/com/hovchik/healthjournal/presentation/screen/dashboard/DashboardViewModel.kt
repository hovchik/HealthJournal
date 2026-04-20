package com.hovchik.healthjournal.presentation.screen.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.domain.model.Symptom
import com.hovchik.healthjournal.domain.model.VitalSign
import com.hovchik.healthjournal.domain.model.VitalType
import com.hovchik.healthjournal.util.displayNameResId
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

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
                        val app = getApplication<Application>()
                        val typeName = app.getString(type.displayNameResId)
                        val symptomsLabel = app.getString(com.hovchik.healthjournal.R.string.correlation_symptoms)
                        val avgLabel = app.getString(com.hovchik.healthjournal.R.string.correlation_avg_format, typeName, highAvg, normalAvg)
                        results.add(
                            CorrelationItem(
                                label = "$typeName ↔ $symptomsLabel",
                                description = avgLabel,
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
