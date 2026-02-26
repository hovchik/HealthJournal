package com.healthjournal.presentation.screen.vitals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthjournal.domain.model.VitalSign
import com.healthjournal.domain.usecase.DeleteVitalSignUseCase
import com.healthjournal.domain.usecase.GetAllVitalSignsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VitalsViewModel @Inject constructor(
    getAllVitalSigns: GetAllVitalSignsUseCase,
    private val deleteVitalSign: DeleteVitalSignUseCase
) : ViewModel() {

    val vitals = getAllVitalSigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeVital(vital: VitalSign) {
        viewModelScope.launch { deleteVitalSign(vital) }
    }
}
