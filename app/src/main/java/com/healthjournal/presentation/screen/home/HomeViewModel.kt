package com.healthjournal.presentation.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.model.VitalSign
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val symptoms = container.getAllSymptoms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentVitals = container.getAllVitalSigns()
        .map { it.take(5) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addNewSymptom(
        name: String,
        intensity: Int,
        durationMinutes: Int?,
        triggers: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            container.addSymptom(
                Symptom(
                    name = name,
                    intensity = intensity,
                    durationMinutes = durationMinutes,
                    triggers = triggers,
                    notes = notes
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun addNewVitalSign(
        type: VitalType,
        value: Double,
        secondaryValue: Double?,
        notes: String
    ) {
        viewModelScope.launch {
            container.addVitalSign(
                VitalSign(
                    type = type,
                    value = value,
                    secondaryValue = secondaryValue,
                    notes = notes
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun removeSymptom(symptom: Symptom) {
        viewModelScope.launch { container.deleteSymptom(symptom) }
    }
}
