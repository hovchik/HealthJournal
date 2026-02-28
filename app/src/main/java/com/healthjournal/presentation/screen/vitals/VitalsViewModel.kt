package com.healthjournal.presentation.screen.vitals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.VitalSign
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VitalsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val activeProfileId = container.userSettingsRepository.getUserSettings()
        .map { it.activeProfileId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val familyMembers = container.familyMemberRepository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vitals = combine(container.getAllVitalSigns(), activeProfileId) { all, profileId ->
        all.filter { it.profileId == profileId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeVital(vital: VitalSign) {
        viewModelScope.launch { container.deleteVitalSign(vital) }
    }
}
