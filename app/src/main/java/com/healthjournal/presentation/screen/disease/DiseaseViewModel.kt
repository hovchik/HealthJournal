package com.healthjournal.presentation.screen.disease

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DiseaseViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val diseases = combine(container.getAllDiseases(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allDiseases = container.getAllDiseases()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allSymptoms = container.getAllSymptoms()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allVitals = container.getAllVitalSigns()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allMedications = container.getAllMedications()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addDisease(name: String, notes: String) {
        viewModelScope.launch {
            val profileId = container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.addDisease(
                Disease(name = name, notes = notes, profileId = profileId)
            )
            _saveSuccess.emit(true)
        }
    }

    suspend fun getDiseaseById(id: Long): Disease? = container.getDiseaseById(id)

    fun updateDisease(disease: Disease) {
        viewModelScope.launch {
            container.updateDisease(disease)
            _saveSuccess.emit(true)
        }
    }

    fun deleteDisease(disease: Disease) {
        viewModelScope.launch { container.deleteDisease(disease) }
    }

    fun getSymptomsForDisease(diseaseId: Long): List<Symptom> =
        allSymptoms.value.filter { it.diseaseId == diseaseId }

    fun getVitalsForDisease(diseaseId: Long): List<VitalSign> =
        allVitals.value.filter { it.diseaseId == diseaseId }

    fun getMedicationsForDisease(diseaseId: Long): List<Medication> =
        allMedications.value.filter { it.diseaseId == diseaseId }
}
