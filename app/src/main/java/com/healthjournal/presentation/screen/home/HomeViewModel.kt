package com.healthjournal.presentation.screen.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.FamilyMember
import com.healthjournal.domain.model.Symptom
import com.healthjournal.domain.model.VitalType
import com.healthjournal.domain.model.VitalSign
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val symptoms = combine(container.getAllSymptoms(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentVitals = combine(container.getAllVitalSigns(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }.take(5)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess = _saveSuccess.asSharedFlow()

    fun addNewSymptom(
        name: String,
        intensity: Int,
        value: String? = null,
        durationMinutes: Int?,
        triggers: List<String>,
        notes: String,
        attachmentPaths: List<String> = emptyList(),
        profileId: Long? = null,
        diseaseId: Long = 0L,
        recordedAt: LocalDateTime = LocalDateTime.now()
    ) {
        viewModelScope.launch {
            val currentProfileId = profileId
                ?: container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.addSymptom(
                Symptom(
                    name = name,
                    intensity = intensity,
                    value = value?.takeIf { it.isNotBlank() },
                    durationMinutes = durationMinutes,
                    triggers = triggers,
                    notes = notes,
                    profileId = currentProfileId,
                    attachmentPaths = attachmentPaths,
                    diseaseId = diseaseId,
                    recordedAt = recordedAt
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
        attachmentPaths: List<String> = emptyList(),
        profileId: Long? = null,
        diseaseId: Long = 0L,
        recordedAt: LocalDateTime = LocalDateTime.now()
    ) {
        viewModelScope.launch {
            val currentProfileId = profileId
                ?: container.userSettingsRepository.getUserSettings().first().activeProfileId
            container.addVitalSign(
                VitalSign(
                    type = type,
                    value = value,
                    secondaryValue = secondaryValue,
                    notes = notes,
                    profileId = currentProfileId,
                    attachmentPaths = attachmentPaths,
                    diseaseId = diseaseId,
                    recordedAt = recordedAt
                )
            )
            _saveSuccess.emit(true)
        }
    }

    suspend fun getSymptomById(id: Long): Symptom? = container.getSymptomById(id)

    fun updateSymptom(symptom: Symptom) {
        viewModelScope.launch {
            container.updateSymptom(symptom)
            _saveSuccess.emit(true)
        }
    }

    suspend fun getVitalSignById(id: Long): VitalSign? = container.getVitalSignById(id)

    fun updateVitalSign(vitalSign: VitalSign) {
        viewModelScope.launch {
            container.updateVitalSign(vitalSign)
            _saveSuccess.emit(true)
        }
    }

    fun removeSymptom(symptom: Symptom) {
        viewModelScope.launch { container.deleteSymptom(symptom) }
    }

    fun getProfileName(profileId: Long): String {
        if (profileId == 0L) return ""
        return familyMembers.value.find { it.id == profileId }?.name ?: ""
    }
}
