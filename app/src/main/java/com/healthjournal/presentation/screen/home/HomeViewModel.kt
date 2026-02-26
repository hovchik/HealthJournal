package com.healthjournal.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.usecase.AddSymptomUseCase
import com.healthjournal.domain.usecase.AddVitalSignUseCase
import com.healthjournal.domain.usecase.DeleteSymptomUseCase
import com.healthjournal.domain.usecase.GetAllSymptomsUseCase
import com.healthjournal.domain.usecase.GetAllVitalSignsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllSymptoms: GetAllSymptomsUseCase,
    private val getAllVitalSigns: GetAllVitalSignsUseCase,
    private val addSymptom: AddSymptomUseCase,
    private val addVitalSign: AddVitalSignUseCase,
    private val deleteSymptom: DeleteSymptomUseCase
) : ViewModel() {

    val symptoms = getAllSymptoms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentVitals = getAllVitalSigns()
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
            addSymptom(
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
            addVitalSign(
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
        viewModelScope.launch { deleteSymptom(symptom) }
    }
}
