package com.hovchik.healthjournal.presentation.screen.subscription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hovchik.healthjournal.HealthJournalApp
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionPlan
import com.hovchik.healthjournal.domain.model.subscription.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val selectedPlan: SubscriptionPlan? = null,
    val isPurchasing: Boolean = false,
    val purchaseSuccess: Boolean = false,
    val errorMessage: String? = null
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as HealthJournalApp).container

    val subscriptionStatus = container.getSubscriptionStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStatus())

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState = _uiState.asStateFlow()

    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun purchasePlan(plan: SubscriptionPlan) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPurchasing = true, errorMessage = null)
            try {
                // TODO: Integrate Google Play Billing here
                // For now, directly set the plan for development/testing
                container.setSubscriptionPlan(plan)
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    purchaseSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPurchasing = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(purchaseSuccess = false)
    }
}
