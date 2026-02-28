package com.healthjournal.presentation.screen.medications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Medication
import com.healthjournal.domain.model.MedicationLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MedicationsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val activeProfileId = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val medications = combine(container.getAllMedications(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addNewMedication(name: String, dosage: String, frequency: String, notes: String, profileId: Long? = null) {
        viewModelScope.launch {
            container.addMedication(
                Medication(
                    name = name,
                    dosage = dosage,
                    frequency = frequency,
                    notes = notes,
                    profileId = profileId ?: activeProfileId.value
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun toggleMedicationActive(medication: Medication) {
        viewModelScope.launch {
            container.updateMedication(medication.copy(active = !medication.active))
        }
    }

    fun logTaken(medication: Medication) {
        viewModelScope.launch {
            container.logMedicationTaken(MedicationLog(medicationId = medication.id))
        }
    }

    fun removeMedication(medication: Medication) {
        viewModelScope.launch { container.deleteMedication(medication) }
    }
}
