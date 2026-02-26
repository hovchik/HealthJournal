package com.healthjournal.presentation.screen.vitals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.HealthJournalApp
import com.healthjournal.domain.model.VitalSign
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VitalsViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as HealthJournalApp).container

    val vitals = container.getAllVitalSigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeVital(vital: VitalSign) {
        viewModelScope.launch { container.deleteVitalSign(vital) }
    }
}
