package com.healthjournal.presentation.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.model.VitalType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val activeProfileId = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val familyMembers = container.familyMemberRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val symptoms = combine(container.getAllSymptoms(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentVitals = combine(container.getAllVitalSigns(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addNewSymptom(
        name: String,
        intensity: Int,
        durationMinutes: Int?,
        triggers: List<String>,
        notes: String,
        profileId: Long = activeProfileId.value,
        attachmentPaths: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            container.addSymptom(
                Symptom(
                    name = name,
                    intensity = intensity,
                    durationMinutes = durationMinutes,
                    triggers = triggers,
                    notes = notes,
                    profileId = profileId,
                    attachmentPaths = attachmentPaths
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun addNewVitalSign(
        type: VitalType,
        value: Double,
        secondaryValue: Double?,
        notes: String,
        attachmentPaths: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            container.addVitalSign(
                VitalSign(
                    type = type,
                    value = value,
                    secondaryValue = secondaryValue,
                    notes = notes,
                    profileId = activeProfileId.value,
                    attachmentPaths = attachmentPaths
                )
            )
            _saveSuccess.emit(true)
        }
    }

    fun removeSymptom(symptom: Symptom) {
        viewModelScope.launch { container.deleteSymptom(symptom) }
    }
}
