package com.healthjournal.presentation.screen.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.MedicationLog
import com.healthjournal.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    getAllMedications: GetAllMedicationsUseCase,
    private val addMedication: AddMedicationUseCase,
    private val updateMedication: UpdateMedicationUseCase,
    private val deleteMedication: DeleteMedicationUseCase,
    private val logMedicationTaken: LogMedicationTakenUseCase
) : ViewModel() {

    val medications = getAllMedications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addNewMedication(name: String, dosage: String, frequency: String, notes: String) {
        viewModelScope.launch {
            addMedication(
                Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    notes = notes
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun toggleMedicationActive(medication: Medication) {
        viewModelScope.launch {
            updateMedication(medication.copy(active = !medication.active))
        }
    }

    fun logTaken(medication: Medication) {
        viewModelScope.launch {
            logMedicationTaken(MedicationLog(medicationId = medication.id))
        }
    }

    fun removeMedication(medication: Medication) {
        viewModelScope.launch { deleteMedication(medication) }
    }
}
