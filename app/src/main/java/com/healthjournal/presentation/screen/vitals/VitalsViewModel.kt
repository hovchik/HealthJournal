package com.healthjournal.presentation.screen.vitals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.Disease
import com.healthjournal.domain.model.VitalSign
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VitalsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    private val activeProfileFlow = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }

    val activeProfileId = activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val vitals = combine(container.getAllVitalSigns(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val diseases = combine(container.getAllDiseases(), activeProfileFlow) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun removeVital(vital: VitalSign) {
        viewModelScope.launch { container.deleteVitalSign(vital) }
    }

    fun getDiseaseName(diseaseId: Long): String? =
        diseases.value.firstOrNull { it.id == diseaseId }?.name
}
